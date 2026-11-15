package ru.quentel.naujava.config;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ru.quentel.naujava.entity.Application;
import ru.quentel.naujava.entity.EventMatch;
import ru.quentel.naujava.entity.EventRule;
import ru.quentel.naujava.entity.LogEntry;
import ru.quentel.naujava.entity.LogFile;
import ru.quentel.naujava.entity.LogLevel;
import ru.quentel.naujava.repository.ApplicationRepository;
import ru.quentel.naujava.repository.EventMatchRepository;
import ru.quentel.naujava.repository.EventRuleRepository;
import ru.quentel.naujava.repository.LogEntryRepository;
import ru.quentel.naujava.repository.LogFileRepository;
import ru.quentel.naujava.repository.LogLevelRepository;

/**
 * Наполнение БД справочником уровней и демонстрационными данными.
 * Отключается свойством app.demo-data.enabled=false (например, в автотестах).
 */
@Component
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataInitializer implements CommandLineRunner {

    private final LogLevelRepository logLevelRepository;
    private final ApplicationRepository applicationRepository;
    private final LogFileRepository logFileRepository;
    private final LogEntryRepository logEntryRepository;
    private final EventRuleRepository eventRuleRepository;
    private final EventMatchRepository eventMatchRepository;

    @Autowired
    public DemoDataInitializer(LogLevelRepository logLevelRepository,
                               ApplicationRepository applicationRepository,
                               LogFileRepository logFileRepository,
                               LogEntryRepository logEntryRepository,
                               EventRuleRepository eventRuleRepository,
                               EventMatchRepository eventMatchRepository) {
        this.logLevelRepository = logLevelRepository;
        this.applicationRepository = applicationRepository;
        this.logFileRepository = logFileRepository;
        this.logEntryRepository = logEntryRepository;
        this.eventRuleRepository = eventRuleRepository;
        this.eventMatchRepository = eventMatchRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Map<String, LogLevel> levels = initLevels();

        if (applicationRepository.count() > 0) {
            System.out.println("Демо-данные уже присутствуют в БД, инициализация пропущена");
            return;
        }

        Application application = applicationRepository.save(
                new Application("nginx-proxy", "NGINX Inc.", "Веб-сервер и обратный прокси"));
        LogFile logFile = logFileRepository.save(
                new LogFile("nginx-error.log", "/var/log/nginx/error.log", 20480L, application));

        String[][] rows = {
                {"INFO", "Application started"},
                {"INFO", "Connection pool initialized"},
                {"WARN", "Connection pool is almost full"},
                {"ERROR", "Connection timeout"},
                {"ERROR", "Connection timeout"},
                {"INFO", "Request processed"},
                {"FATAL", "Out of memory"}
        };

        LogEntry firstError = null;
        int lineNumber = 1;
        for (String[] row : rows) {
            LogEntry entry = new LogEntry();
            entry.setLogFile(logFile);
            entry.setLevel(levels.get(row[0]));
            entry.setMessage(row[1]);
            entry.setLineNumber(lineNumber);
            entry.setEventTime(LocalDateTime.now().minusMinutes(rows.length - lineNumber));
            entry.setRawLine(entry.getEventTime() + " [" + row[0] + "] " + row[1]);
            entry = logEntryRepository.save(entry);
            if (firstError == null && "ERROR".equals(row[0])) {
                firstError = entry;
            }
            lineNumber++;
        }

        EventRule rule = eventRuleRepository.save(new EventRule(
                "Таймауты соединения", "Connection timeout", "Сетевые ошибки", true, application));
        eventMatchRepository.save(new EventMatch(rule, firstError, "Connection timeout", 0.95));

        System.out.println("Демо-данные загружены: приложение '" + application.getName()
                + "', файл '" + logFile.getFileName() + "', записей: " + rows.length);
    }

    private Map<String, LogLevel> initLevels() {
        Map<String, Integer> dictionary = new LinkedHashMap<>();
        dictionary.put("TRACE", 1);
        dictionary.put("DEBUG", 2);
        dictionary.put("INFO", 3);
        dictionary.put("WARN", 4);
        dictionary.put("ERROR", 5);
        dictionary.put("FATAL", 6);

        Map<String, LogLevel> levels = new LinkedHashMap<>();
        dictionary.forEach((name, severity) -> {
            LogLevel level = logLevelRepository.findByName(name)
                    .orElseGet(() -> logLevelRepository.save(new LogLevel(name, severity, "Уровень " + name)));
            levels.put(name, level);
        });
        return levels;
    }
}