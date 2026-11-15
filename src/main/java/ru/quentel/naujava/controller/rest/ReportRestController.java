package ru.quentel.naujava.controller.rest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ru.quentel.naujava.dto.ReportDto;
import ru.quentel.naujava.entity.Report;
import ru.quentel.naujava.entity.ReportStatus;
import ru.quentel.naujava.service.ReportService;

@RestController
@RequestMapping("/custom/reports")
@Tag(name = "Отчёты", description = "Асинхронное формирование HTML-отчёта со статистикой приложения")
public class ReportRestController {

    private final ReportService reportService;

    @Autowired
    public ReportRestController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Создать отчёт и запустить формирование (не дожидаясь окончания)")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create() {
        Long reportId = reportService.createReport();
        // вызов через прокси Spring -> метод выполняется асинхронно, ответ отдаётся сразу
        reportService.generateReportAsync(reportId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.<String, Object>of(
                        "reportId", reportId,
                        "status", ReportStatus.CREATED.name(),
                        "message", "Отчёт создан и поставлен на формирование"));
    }

    @Operation(summary = "Статус и содержимое отчёта по id")
    @GetMapping("/{id}")
    public ReportDto get(@PathVariable Long id) {
        return ReportDto.from(reportService.getReport(id));
    }

    @Operation(summary = "HTML-страница отчёта (или сообщение о том, что он ещё формируется)")
    @GetMapping(value = "/{id}/content", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    public String content(@PathVariable Long id) {
        Report report = reportService.getReport(id);
        return switch (report.getStatus()) {
            case COMPLETED -> reportService.getReportContent(id);
            case CREATED -> placeholder("Отчёт №" + id + " ещё формируется",
                    "Обновите страницу через несколько секунд.");
            case ERROR -> placeholder("Отчёт №" + id + " завершился с ошибкой", report.getErrorMessage());
        };
    }

    @Operation(summary = "Список всех отчётов")
    @GetMapping
    public List<ReportDto> all() {
        return reportService.findAll().stream().map(ReportDto::summary).toList();
    }

    private String placeholder(String title, String details) {
        return "<!DOCTYPE html><html lang=\"ru\"><head><meta charset=\"UTF-8\"><title>" + title
                + "</title></head><body style=\"font-family:Segoe UI,Arial,sans-serif;margin:40px;\">"
                + "<h1>" + title + "</h1><p>" + (details == null ? "" : details) + "</p>"
                + "<p><a href=\"javascript:location.reload()\">Обновить</a></p></body></html>";
    }
}