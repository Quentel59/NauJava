package ru.quentel.naujava.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Факт срабатывания правила на конкретной записи лога.
 * Промежуточная сущность между EventRule и LogEntry (вместо ManyToMany).
 */
@Entity
@Table(name = "event_matches")
public class EventMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Когда зафиксировано совпадение. */
    @Column(name = "matched_at", nullable = false)
    private LocalDateTime matchedAt;

    /** Совпавший фрагмент строки. */
    @Column(name = "matched_fragment", length = 1000)
    private String matchedFragment;

    /** Достоверность совпадения (0..1). */
    @Column(name = "confidence")
    private Double confidence;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private EventRule rule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "log_entry_id", nullable = false)
    private LogEntry logEntry;

    public EventMatch() {
    }

    public EventMatch(EventRule rule, LogEntry logEntry, String matchedFragment, Double confidence) {
        this.rule = rule;
        this.logEntry = logEntry;
        this.matchedFragment = matchedFragment;
        this.confidence = confidence;
        this.matchedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(LocalDateTime matchedAt) {
        this.matchedAt = matchedAt;
    }

    public String getMatchedFragment() {
        return matchedFragment;
    }

    public void setMatchedFragment(String matchedFragment) {
        this.matchedFragment = matchedFragment;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public EventRule getRule() {
        return rule;
    }

    public void setRule(EventRule rule) {
        this.rule = rule;
    }

    public LogEntry getLogEntry() {
        return logEntry;
    }

    public void setLogEntry(LogEntry logEntry) {
        this.logEntry = logEntry;
    }

    @Override
    public String toString() {
        return "EventMatch{id=" + id + ", matchedFragment='" + matchedFragment + "', confidence=" + confidence + "}";
    }
}