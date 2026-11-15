package ru.quentel.naujava.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Представление записи лога для REST и HTML.
 * Отдаём DTO, а не сущность: не тянем ленивые прокси Hibernate в сериализацию.
 */
public class LogEntryDto {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Long id;
    private final LocalDateTime eventTime;
    private final String level;
    private final String message;
    private final Integer lineNumber;
    private final String fileName;
    private final String applicationName;

    public LogEntryDto(Long id, LocalDateTime eventTime, String level, String message,
                       Integer lineNumber, String fileName, String applicationName) {
        this.id = id;
        this.eventTime = eventTime;
        this.level = level;
        this.message = message;
        this.lineNumber = lineNumber;
        this.fileName = fileName;
        this.applicationName = applicationName;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    /** Готовая строка времени для HTML-таблицы. */
    public String getEventTimeText() {
        return eventTime == null ? "-" : eventTime.format(FORMATTER);
    }
}