package ru.quentel.naujava.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Справочник уровней логирования: TRACE, DEBUG, INFO, WARN, ERROR, FATAL.
 */
@Entity
@Table(name = "log_levels")
public class LogLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Название уровня. */
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private String name;

    /** Числовая важность (чем больше, тем критичнее). */
    @Column(name = "severity", nullable = false)
    private Integer severity;

    /** Описание уровня. */
    @Column(name = "description", length = 200)
    private String description;

    @OneToMany(mappedBy = "level")
    private List<LogEntry> entries = new ArrayList<>();

    public LogLevel() {
    }

    public LogLevel(String name, Integer severity, String description) {
        this.name = name;
        this.severity = severity;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<LogEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<LogEntry> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return "LogLevel{id=" + id + ", name='" + name + "', severity=" + severity + "}";
    }
}