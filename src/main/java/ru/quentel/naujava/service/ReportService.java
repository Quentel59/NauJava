package ru.quentel.naujava.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import ru.quentel.naujava.entity.Report;

public interface ReportService {

    /** Создаёт отчёт в БД со статусом CREATED и возвращает его id. */
    Long createReport();

    /** Возвращает отчёт по идентификатору. */
    Report getReport(Long id);

    /** Возвращает содержимое отчёта (HTML) или null, если оно ещё не сформировано. */
    String getReportContent(Long id);

    /** Список отчётов от новых к старым. */
    List<Report> findAll();

    /** Асинхронно формирует отчёт в несколько потоков. */
    CompletableFuture<Void> generateReportAsync(Long reportId);
}