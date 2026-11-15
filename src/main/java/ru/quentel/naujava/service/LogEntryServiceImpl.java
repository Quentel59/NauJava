package ru.quentel.naujava.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.quentel.naujava.entity.LogEntry;
import ru.quentel.naujava.entity.LogFile;
import ru.quentel.naujava.entity.LogLevel;
import ru.quentel.naujava.repository.LogEntryRepository;
import ru.quentel.naujava.repository.LogFileRepository;
import ru.quentel.naujava.repository.LogLevelRepository;

@Service
@Transactional(readOnly = true)
public class LogEntryServiceImpl implements LogEntryService {

    private final LogEntryRepository logEntryRepository;
    private final LogFileRepository logFileRepository;
    private final LogLevelRepository logLevelRepository;

    @Autowired
    public LogEntryServiceImpl(LogEntryRepository logEntryRepository,
                               LogFileRepository logFileRepository,
                               LogLevelRepository logLevelRepository) {
        this.logEntryRepository = logEntryRepository;
        this.logFileRepository = logFileRepository;
        this.logLevelRepository = logLevelRepository;
    }

    @Override
    @Transactional
    public LogEntry addLogEntry(Long logFileId, String levelName, String message, Integer lineNumber) {
        if (logFileId == null) {
            throw new IllegalArgumentException("Не указан идентификатор файла логов");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Сообщение лога не может быть пустым");
        }
        if (lineNumber != null && lineNumber <= 0) {
            throw new IllegalArgumentException("Номер строки должен быть больше нуля");
        }
        String normalizedLevel = normalizeLevel(levelName);

        LogFile logFile = logFileRepository.findById(logFileId)
                .orElseThrow(() -> new IllegalArgumentException("Файл логов с id=" + logFileId + " не найден"));
        LogLevel level = logLevelRepository.findByName(normalizedLevel)
                .orElseThrow(() -> new IllegalArgumentException("Уровень '" + normalizedLevel + "' отсутствует в справочнике"));

        LogEntry entry = new LogEntry();
        entry.setLogFile(logFile);
        entry.setLevel(level);
        entry.setMessage(message.trim());
        entry.setLineNumber(lineNumber);
        entry.setEventTime(LocalDateTime.now());
        entry.setRawLine(entry.getEventTime() + " " + level.getName() + " " + entry.getMessage());

        return logEntryRepository.save(entry);
    }

    @Override
    public LogEntry findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id не может быть null");
        }
        return logEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Запись лога с id=" + id + " не найдена"));
    }

    @Override
    public List<LogEntry> findAll() {
        return logEntryRepository.findAll();
    }

    @Override
    @Transactional
    public void updateMessage(Long id, String newMessage) {
        if (newMessage == null || newMessage.isBlank()) {
            throw new IllegalArgumentException("Новое сообщение не может быть пустым");
        }
        LogEntry entry = findById(id);
        entry.setMessage(newMessage.trim());
        logEntryRepository.save(entry);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id);
        logEntryRepository.deleteById(id);
    }

    @Override
    public Map<String, Long> countByLevel() {
        return logEntryRepository.findAll().stream()
                .collect(Collectors.groupingBy(e -> e.getLevel().getName(), TreeMap::new, Collectors.counting()));
    }

    @Override
    public Map<String, Long> topFrequentMessages(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Размер топа должен быть больше нуля");
        }
        Map<String, Long> counts = logEntryRepository.findAll().stream()
                .collect(Collectors.groupingBy(LogEntry::getMessage, Collectors.counting()));

        Comparator<Map.Entry<String, Long>> byCountDesc =
                Comparator.comparingLong((Map.Entry<String, Long> e) -> e.getValue()).reversed();

        return counts.entrySet().stream()
                .sorted(byCountDesc.thenComparing(Map.Entry::getKey))
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    @Override
    public Map<String, List<LogEntry>> groupByLevel() {
        return logEntryRepository.findAll().stream()
                .collect(Collectors.groupingBy(e -> e.getLevel().getName(), TreeMap::new, Collectors.toList()));
    }

    private String normalizeLevel(String levelName) {
        if (levelName == null || levelName.isBlank()) {
            throw new IllegalArgumentException("Уровень логирования не может быть пустым");
        }
        return levelName.trim().toUpperCase(Locale.ROOT);
    }
}