package ru.quentel.naujava.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Сущность предметной области.
 */
public class LogEntry {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Уникальный идентификатор записи. */
    private Long id;

    /** Уровень логирования: TRACE, DEBUG, INFO, WARN, ERROR, FATAL. */
    private String level;

    /** Текст сообщения лога. */
    private String message;

    /** Время события. */
    private LocalDateTime timestamp;

    public LogEntry() {
    }

    public LogEntry(Long id, String level, String message, LocalDateTime timestamp) {
        this.id = id;
        this.level = level;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s %-5s %s",
                id,
                timestamp == null ? "-" : timestamp.format(FORMATTER),
                level,
                message);
    }
}