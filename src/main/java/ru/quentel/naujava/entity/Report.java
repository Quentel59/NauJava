package ru.quentel.naujava.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Отчёт со статистикой приложения. Содержимое — готовая HTML-страница.
 */
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Статус формирования (хранится строкой). */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status;

    /** HTML-содержимое отчёта. В PostgreSQL — колонка типа text. */
    @Column(name = "content", columnDefinition = "text")
    private String content;

    /** Момент создания отчёта. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Сообщение об ошибке, если статус ERROR. */
    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    /** Общее время формирования, мс. */
    @Column(name = "duration_ms")
    private Long durationMs;

    public Report() {
    }

    public Report(ReportStatus status) {
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    @Override
    public String toString() {
        return "Report{id=" + id + ", status=" + status + ", durationMs=" + durationMs + "}";
    }
}