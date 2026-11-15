package ru.quentel.naujava.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import ru.quentel.naujava.dao.CrudRepository;
import ru.quentel.naujava.entity.LogEntry;

/**
 * Реализация бизнес-логики анализа логов.
 */
@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class LogEntryServiceImpl implements LogEntryService {

    private static final Set<String> ALLOWED_LEVELS =
            Set.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL");

    private final CrudRepository<LogEntry, Long> logEntryRepository;

    @Autowired
    public LogEntryServiceImpl(CrudRepository<LogEntry, Long> logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    @Override
    public LogEntry addLogEntry(String level, String message) {
        String normalizedLevel = normalizeLevel(level);
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Сообщение лога не может быть пустым");
        }
        LogEntry entry = new LogEntry(null, normalizedLevel, message.trim(), LocalDateTime.now());
        logEntryRepository.create(entry);
        return entry;
    }

    @Override
    public LogEntry findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id не может быть null");
        }
        LogEntry entry = logEntryRepository.read(id);
        if (entry == null) {
            throw new IllegalArgumentException("Запись лога с id=" + id + " не найдена");
        }
        return entry;
    }

    @Override
    public List<LogEntry> findAll() {
        return logEntryRepository.readAll();
    }

    @Override
    public void updateMessage(Long id, String newMessage) {
        if (newMessage == null || newMessage.isBlank()) {
            throw new IllegalArgumentException("Новое сообщение не может быть пустым");
        }
        LogEntry entry = findById(id);
        entry.setMessage(newMessage.trim());
        logEntryRepository.update(entry);
    }

    @Override
    public void deleteById(Long id) {
        findById(id);
        logEntryRepository.delete(id);
    }

    @Override
    public Map<String, Long> countByLevel() {
        return logEntryRepository.readAll().stream()
                .collect(Collectors.groupingBy(LogEntry::getLevel, TreeMap::new, Collectors.counting()));
    }

    @Override
    public Map<String, Long> topFrequentMessages(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Размер топа должен быть больше 0");
        }
        Map<String, Long> counts = logEntryRepository.readAll().stream()
                .collect(Collectors.groupingBy(LogEntry::getMessage, Collectors.counting()));

        Comparator<Map.Entry<String, Long>> byCountDesc =
                Comparator.comparingLong((Map.Entry<String, Long> e) -> e.getValue()).reversed();

        return counts.entrySet().stream()
                .sorted(byCountDesc.thenComparing(Map.Entry::getKey))
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    @Override
    public Map<String, List<LogEntry>> groupByLevel() {
        return logEntryRepository.readAll().stream()
                .collect(Collectors.groupingBy(LogEntry::getLevel, TreeMap::new, Collectors.toList()));
    }

    private String normalizeLevel(String level) {
        if (level == null || level.isBlank()) {
            throw new IllegalArgumentException("Уровень логирования не может быть пустым");
        }
        String normalized = level.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_LEVELS.contains(normalized)) {
            throw new IllegalArgumentException(
                    "Неизвестный уровень '" + level + "'. Допустимые: " + ALLOWED_LEVELS);
        }
        return normalized;
    }
}