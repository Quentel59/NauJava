package ru.quentel.naujava.entity;

/**
 * Статусы формирования отчёта.
 */
public enum ReportStatus {

    CREATED("Создан"),
    COMPLETED("Завершен"),
    ERROR("Ошибка");

    private final String title;

    ReportStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}