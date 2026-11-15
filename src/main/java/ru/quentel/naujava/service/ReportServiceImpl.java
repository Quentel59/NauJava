package ru.quentel.naujava.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.quentel.naujava.dto.LogEntryDto;
import ru.quentel.naujava.dto.LogEntryDtoMapper;
import ru.quentel.naujava.entity.LogEntry;
import ru.quentel.naujava.entity.Report;
import ru.quentel.naujava.entity.ReportStatus;
import ru.quentel.naujava.exception.ResourceNotFoundException;
import ru.quentel.naujava.repository.AppUserRepository;
import ru.quentel.naujava.repository.LogEntryRepository;
import ru.quentel.naujava.repository.ReportRepository;

/**
 * Формирование HTML-отчёта со статистикой приложения.
 * Подсчёт пользователей и выборка записей логов выполняются в двух отдельных потоках.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int TOP_LIMIT = 5;

    private final ReportRepository reportRepository;
    private final AppUserRepository appUserRepository;
    private final LogEntryRepository logEntryRepository;
    private final LogEntryDtoMapper logEntryDtoMapper;

    @Autowired
    public ReportServiceImpl(ReportRepository reportRepository,
                             AppUserRepository appUserRepository,
                             LogEntryRepository logEntryRepository,
                             LogEntryDtoMapper logEntryDtoMapper) {
        this.reportRepository = reportRepository;
        this.appUserRepository = appUserRepository;
        this.logEntryRepository = logEntryRepository;
        this.logEntryDtoMapper = logEntryDtoMapper;
    }

    @Override
    @Transactional
    public Long createReport() {
        Report saved = reportRepository.save(new Report(ReportStatus.CREATED));
        log.info("Создан отчёт id={} со статусом {}", saved.getId(), saved.getStatus());
        return saved.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Report getReport(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id отчёта не может быть null");
        }
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Отчёт с id=" + id + " не найден"));
    }

    @Override
    @Transactional(readOnly = true)
    public String getReportContent(Long id) {
        return getReport(id).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> findAll() {
        return reportRepository.findAllByOrderByIdDesc();
    }

    @Async("reportExecutor")
    @Override
    public CompletableFuture<Void> generateReportAsync(Long reportId) {
        long totalStart = System.currentTimeMillis();

        AtomicLong usersCount = new AtomicLong();
        AtomicLong usersTime = new AtomicLong();
        AtomicLong entriesTime = new AtomicLong();
        AtomicReference<EntriesData> entriesData = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();

        // Поток №1: количество зарегистрированных пользователей
        Thread usersThread = new Thread(() -> {
            long start = System.currentTimeMillis();
            try {
                usersCount.set(appUserRepository.count());
            } catch (RuntimeException ex) {
                failure.compareAndSet(null, ex);
            } finally {
                usersTime.set(System.currentTimeMillis() - start);
                log.info("Отчёт {}: пользователи посчитаны в потоке '{}' за {} мс",
                        reportId, Thread.currentThread().getName(), usersTime.get());
            }
        }, "report-users-" + reportId);

        // Поток №2: список записей логов + статистика по ним
        Thread entriesThread = new Thread(() -> {
            long start = System.currentTimeMillis();
            try {
                List<LogEntry> found = logEntryRepository.findAll();
                entriesData.set(new EntriesData(
                        logEntryDtoMapper.toDtoList(found),
                        countByLevel(found),
                        topMessages(found)));
            } catch (RuntimeException ex) {
                failure.compareAndSet(null, ex);
            } finally {
                entriesTime.set(System.currentTimeMillis() - start);
                log.info("Отчёт {}: записи логов получены в потоке '{}' за {} мс",
                        reportId, Thread.currentThread().getName(), entriesTime.get());
            }
        }, "report-entries-" + reportId);

        usersThread.start();
        entriesThread.start();

        try {
            // дожидаемся завершения обоих потоков
            usersThread.join();
            entriesThread.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            failure.compareAndSet(null, ex);
        }

        long totalTime = System.currentTimeMillis() - totalStart;
        saveResult(reportId, usersCount.get(), usersTime.get(), entriesData.get(),
                entriesTime.get(), totalTime, failure.get());

        return CompletableFuture.completedFuture(null);
    }

    /** Записывает результат формирования в БД: COMPLETED или ERROR. */
    private void saveResult(Long reportId, long usersCount, long usersTime, EntriesData data,
                            long entriesTime, long totalTime, Throwable failure) {
        try {
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ResourceNotFoundException("Отчёт с id=" + reportId + " не найден"));
            report.setDurationMs(totalTime);

            if (failure != null || data == null) {
                String message = failure == null ? "Не удалось получить данные для отчёта" : failure.getMessage();
                report.setStatus(ReportStatus.ERROR);
                report.setErrorMessage(truncate(message));
                report.setContent(errorHtml(reportId, report.getErrorMessage()));
                log.error("Отчёт {} завершился с ошибкой: {}", reportId, report.getErrorMessage());
            } else {
                report.setContent(buildHtml(reportId, usersCount, usersTime, data, entriesTime, totalTime));
                report.setStatus(ReportStatus.COMPLETED);
                log.info("Отчёт {} сформирован за {} мс (пользователи: {} мс, записи: {} мс)",
                        reportId, totalTime, usersTime, entriesTime);
            }
            reportRepository.save(report);
        } catch (RuntimeException ex) {
            log.error("Не удалось сохранить результат отчёта {}: {}", reportId, ex.getMessage(), ex);
        }
    }

    private Map<String, Long> countByLevel(List<LogEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(e -> e.getLevel().getName(), TreeMap::new, Collectors.counting()));
    }

    private Map<String, Long> topMessages(List<LogEntry> entries) {
        Map<String, Long> counts = entries.stream()
                .collect(Collectors.groupingBy(LogEntry::getMessage, Collectors.counting()));

        Comparator<Map.Entry<String, Long>> byCountDesc =
                Comparator.comparingLong((Map.Entry<String, Long> e) -> e.getValue()).reversed();

        return counts.entrySet().stream()
                .sorted(byCountDesc.thenComparing(Map.Entry::getKey))
                .limit(TOP_LIMIT)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    /** Сборка человекочитаемой HTML-страницы отчёта. */
    private String buildHtml(Long reportId, long usersCount, long usersTime,
                             EntriesData data, long entriesTime, long totalTime) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"ru\"><head><meta charset=\"UTF-8\">")
                .append("<title>Отчёт №").append(reportId).append("</title><style>")
                .append("body{font-family:Segoe UI,Arial,sans-serif;margin:24px;color:#222;}")
                .append("h1{font-size:22px;}h2{font-size:17px;margin-top:26px;}")
                .append("table{border-collapse:collapse;margin-top:8px;}")
                .append("th,td{border:1px solid #bbb;padding:6px 10px;font-size:14px;text-align:left;}")
                .append("th{background:#f0f0f0;}")
                .append("</style></head><body>");

        html.append("<h1>Отчёт по анализу логов №").append(reportId).append("</h1>")
                .append("<p>Сформирован: ").append(LocalDateTime.now().format(FORMATTER)).append("</p>");

        // Требование 2: количество зарегистрированных пользователей
        html.append("<h2>1. Пользователи системы</h2><table>")
                .append("<tr><th>Показатель</th><th>Значение</th></tr>")
                .append("<tr><td>Количество зарегистрированных пользователей</td><td>")
                .append(usersCount).append("</td></tr></table>");

        // Требование 3: список объектов выбранной сущности
        html.append("<h2>2. Записи логов (").append(data.entries().size()).append(")</h2>")
                .append("<table><tr><th>ID</th><th>Время</th><th>Уровень</th>")
                .append("<th>Сообщение</th><th>Файл</th><th>Приложение</th></tr>");
        for (LogEntryDto entry : data.entries()) {
            html.append("<tr><td>").append(entry.getId())
                    .append("</td><td>").append(escape(entry.getEventTimeText()))
                    .append("</td><td>").append(escape(entry.getLevel()))
                    .append("</td><td>").append(escape(entry.getMessage()))
                    .append("</td><td>").append(escape(entry.getFileName()))
                    .append("</td><td>").append(escape(entry.getApplicationName()))
                    .append("</td></tr>");
        }
        if (data.entries().isEmpty()) {
            html.append("<tr><td colspan=\"6\">Записей логов нет</td></tr>");
        }
        html.append("</table>");

        html.append("<h2>2.1. Статистика по уровням логирования</h2>");
        appendMapTable(html, "Уровень", "Количество", data.levelStats());

        html.append("<h2>2.2. Топ-").append(TOP_LIMIT).append(" сообщений по частоте встречаемости</h2>");
        appendMapTable(html, "Сообщение", "Частота", data.topMessages());

        // Требования 4, 5, 6: затраченное время
        html.append("<h2>3. Затраченное время</h2><table>")
                .append("<tr><th>Этап</th><th>Поток</th><th>Время, мс</th></tr>")
                .append("<tr><td>Подсчёт количества пользователей</td><td>report-users-").append(reportId)
                .append("</td><td>").append(usersTime).append("</td></tr>")
                .append("<tr><td>Получение списка записей логов</td><td>report-entries-").append(reportId)
                .append("</td><td>").append(entriesTime).append("</td></tr>")
                .append("<tr><td><b>Общее время формирования отчёта</b></td><td>")
                .append(escape(Thread.currentThread().getName()))
                .append("</td><td><b>").append(totalTime).append("</b></td></tr>")
                .append("</table>");

        html.append("</body></html>");
        return html.toString();
    }

    private void appendMapTable(StringBuilder html, String keyTitle, String valueTitle, Map<String, Long> values) {
        html.append("<table><tr><th>").append(keyTitle).append("</th><th>").append(valueTitle).append("</th></tr>");
        values.forEach((key, value) -> html.append("<tr><td>").append(escape(key))
                .append("</td><td>").append(value).append("</td></tr>"));
        if (values.isEmpty()) {
            html.append("<tr><td colspan=\"2\">Нет данных</td></tr>");
        }
        html.append("</table>");
    }

    private String errorHtml(Long reportId, String message) {
        return "<!DOCTYPE html><html lang=\"ru\"><head><meta charset=\"UTF-8\"><title>Отчёт №" + reportId
                + "</title></head><body><h1>Отчёт №" + reportId + " завершился с ошибкой</h1><p>"
                + escape(message) + "</p></body></html>";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String truncate(String value) {
        if (value == null) {
            return "Неизвестная ошибка";
        }
        return value.length() > 2000 ? value.substring(0, 2000) : value;
    }

    /** Результат работы второго потока. */
    private record EntriesData(List<LogEntryDto> entries,
                               Map<String, Long> levelStats,
                               Map<String, Long> topMessages) {
    }
}