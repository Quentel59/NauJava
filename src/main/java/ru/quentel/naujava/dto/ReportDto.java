package ru.quentel.naujava.dto;

import java.time.LocalDateTime;

import ru.quentel.naujava.entity.Report;
import ru.quentel.naujava.entity.ReportStatus;

/**
 * Представление отчёта. Содержимое отдаётся только для завершённого отчёта.
 */
public class ReportDto {

    private final Long id;
    private final String status;
    private final String statusTitle;
    private final LocalDateTime createdAt;
    private final Long durationMs;
    private final String message;
    private final String content;

    private ReportDto(Long id, String status, String statusTitle, LocalDateTime createdAt,
                      Long durationMs, String message, String content) {
        this.id = id;
        this.status = status;
        this.statusTitle = statusTitle;
        this.createdAt = createdAt;
        this.durationMs = durationMs;
        this.message = message;
        this.content = content;
    }

    /** Полное представление: с содержимым, если отчёт готов. */
    public static ReportDto from(Report report) {
        return new ReportDto(report.getId(), report.getStatus().name(), report.getStatus().getTitle(),
                report.getCreatedAt(), report.getDurationMs(), messageFor(report),
                report.getStatus() == ReportStatus.COMPLETED ? report.getContent() : null);
    }

    /** Краткое представление для списка отчётов (без тяжёлого HTML). */
    public static ReportDto summary(Report report) {
        return new ReportDto(report.getId(), report.getStatus().name(), report.getStatus().getTitle(),
                report.getCreatedAt(), report.getDurationMs(), messageFor(report), null);
    }

    private static String messageFor(Report report) {
        return switch (report.getStatus()) {
            case CREATED -> "Отчёт ещё формируется, повторите запрос позже";
            case ERROR -> "Отчёт завершился с ошибкой: " + report.getErrorMessage();
            case COMPLETED -> "Отчёт готов";
        };
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusTitle() {
        return statusTitle;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public String getMessage() {
        return message;
    }

    public String getContent() {
        return content;
    }
}