package ru.quentel.naujava.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Одна запись (строка) лога. Не существует без файла лога и уровня логирования.
 */
@Entity
@Table(name = "log_entries")
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Текст сообщения. */
    @Column(name = "message", nullable = false, length = 4000)
    private String message;

    /** Исходная строка файла лога. */
    @Column(name = "raw_line", length = 4000)
    private String rawLine;

    /** Номер строки в файле. */
    @Column(name = "line_number")
    private Integer lineNumber;

    /** Время события. */
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "log_file_id", nullable = false)
    private LogFile logFile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "level_id", nullable = false)
    private LogLevel level;

    @OneToMany(mappedBy = "logEntry")
    private List<EventMatch> matches = new ArrayList<>();

    public LogEntry() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRawLine() {
        return rawLine;
    }

    public void setRawLine(String rawLine) {
        this.rawLine = rawLine;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public LogFile getLogFile() {
        return logFile;
    }

    public void setLogFile(LogFile logFile) {
        this.logFile = logFile;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public List<EventMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<EventMatch> matches) {
        this.matches = matches;
    }

    @Override
    public String toString() {
        return "LogEntry{id=" + id + ", eventTime=" + eventTime + ", message='" + message + "'}";
    }
}