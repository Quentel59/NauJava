package ru.quentel.naujava.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ru.quentel.naujava.exception.ResourceNotFoundException;
import ru.quentel.naujava.repository.LogFileRepository;
import ru.quentel.naujava.service.LogFileService;

/**
 * REST-доступ к транзакционной операции.
 */
@RestController
@RequestMapping("/custom/log-files")
@Tag(name = "Файлы логов", description = "Транзакционное удаление файла логов вместе с записями")
public class LogFileRestController {

    private final LogFileService logFileService;
    private final LogFileRepository logFileRepository;

    @Autowired
    public LogFileRestController(LogFileService logFileService, LogFileRepository logFileRepository) {
        this.logFileService = logFileService;
        this.logFileRepository = logFileRepository;
    }

    @Operation(summary = "Удалить файл логов и все его записи (одна транзакция)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWithEntries(@PathVariable Long id) {
        if (!logFileRepository.existsById(id)) {
            throw new ResourceNotFoundException("Файл логов с id=" + id + " не найден");
        }
        logFileService.deleteLogFileWithEntries(id);
    }
}