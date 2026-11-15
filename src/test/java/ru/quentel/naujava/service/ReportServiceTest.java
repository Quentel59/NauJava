package ru.quentel.naujava.service;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ru.quentel.naujava.entity.Report;
import ru.quentel.naujava.entity.ReportStatus;
import ru.quentel.naujava.exception.ResourceNotFoundException;

@SpringBootTest
@ActiveProfiles("test")
class ReportServiceTest {

    private final ReportService reportService;

    @Autowired
    ReportServiceTest(ReportService reportService) {
        this.reportService = reportService;
    }

    /** Создание отчёта: статус CREATED, содержимого ещё нет. */
    @Test
    void testCreateReport() {
        Long id = reportService.createReport();
        Report report = reportService.getReport(id);

        Assertions.assertNotNull(id);
        Assertions.assertEquals(ReportStatus.CREATED, report.getStatus());
        Assertions.assertNull(report.getContent());
    }

    /** Асинхронное формирование: статус COMPLETED и все обязательные разделы в HTML. */
    @Test
    void testGenerateReport() throws Exception {
        Long id = reportService.createReport();
        reportService.generateReportAsync(id).get(30, TimeUnit.SECONDS);

        Report report = reportService.getReport(id);
        String content = reportService.getReportContent(id);

        Assertions.assertEquals(ReportStatus.COMPLETED, report.getStatus());
        Assertions.assertNotNull(content);
        Assertions.assertNotNull(report.getDurationMs());
        Assertions.assertTrue(content.contains("<table>"));
        Assertions.assertTrue(content.contains("Количество зарегистрированных пользователей"));
        Assertions.assertTrue(content.contains("Записи логов"));
        Assertions.assertTrue(content.contains("Подсчёт количества пользователей"));
        Assertions.assertTrue(content.contains("Получение списка записей логов"));
        Assertions.assertTrue(content.contains("Общее время формирования отчёта"));
    }

    /** Негативный кейс: несуществующий отчёт. */
    @Test
    void testUnknownReport() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> reportService.getReport(-1L));
    }
}