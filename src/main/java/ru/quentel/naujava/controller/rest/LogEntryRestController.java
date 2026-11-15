package ru.quentel.naujava.controller.rest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ru.quentel.naujava.criteria.LogEntryCriteriaRepository;
import ru.quentel.naujava.dto.LogEntryDto;
import ru.quentel.naujava.dto.LogEntryDtoMapper;
import ru.quentel.naujava.entity.LogEntry;
import ru.quentel.naujava.exception.ResourceNotFoundException;
import ru.quentel.naujava.repository.LogEntryRepository;
import ru.quentel.naujava.service.LogEntryService;

/**
 * REST-доступ к кастомным методам репозитория (Spring Data и Criteria API)
 * и к статистике анализа логов.
 */
@RestController
@RequestMapping("/custom/log-entries")
@Tag(name = "Записи логов", description = "Кастомные запросы и статистика по записям логов")
public class LogEntryRestController {

    private final LogEntryRepository logEntryRepository;
    private final LogEntryCriteriaRepository logEntryCriteriaRepository;
    private final LogEntryService logEntryService;
    private final LogEntryDtoMapper mapper;

    @Autowired
    public LogEntryRestController(LogEntryRepository logEntryRepository,
                                  LogEntryCriteriaRepository logEntryCriteriaRepository,
                                  LogEntryService logEntryService,
                                  LogEntryDtoMapper mapper) {
        this.logEntryRepository = logEntryRepository;
        this.logEntryCriteriaRepository = logEntryCriteriaRepository;
        this.logEntryService = logEntryService;
        this.mapper = mapper;
    }

    @Operation(summary = "Записи заданного уровня за период (Query Method: And + Between)")
    @GetMapping("/findByLevelAndPeriod")
    public List<LogEntryDto> findByLevelAndPeriod(
            @RequestParam String level,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        checkPeriod(from, to);
        return mapper.toDtoList(logEntryRepository.findByLevelNameAndEventTimeBetween(normalize(level), from, to));
    }

    @Operation(summary = "Записи логов приложения (JPQL через LogFile -> Application)")
    @GetMapping("/findByApplication")
    public List<LogEntryDto> findByApplication(@RequestParam String applicationName) {
        return mapper.toDtoList(logEntryRepository.findByApplicationName(requireText(applicationName, "applicationName")));
    }

    @Operation(summary = "То же, что findByLevelAndPeriod, но через Criteria API")
    @GetMapping("/criteria/findByLevelAndPeriod")
    public List<LogEntryDto> criteriaFindByLevelAndPeriod(
            @RequestParam String level,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        checkPeriod(from, to);
        return mapper.toDtoList(logEntryCriteriaRepository.findByLevelNameAndEventTimeBetween(normalize(level), from, to));
    }

    @Operation(summary = "То же, что findByApplication, но через Criteria API")
    @GetMapping("/criteria/findByApplication")
    public List<LogEntryDto> criteriaFindByApplication(@RequestParam String applicationName) {
        return mapper.toDtoList(logEntryCriteriaRepository.findByApplicationName(requireText(applicationName, "applicationName")));
    }

    @Operation(summary = "Запись лога по идентификатору")
    @GetMapping("/{id}")
    public LogEntryDto findById(@PathVariable Long id) {
        LogEntry entry = logEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Запись лога с id=" + id + " не найдена"));
        return mapper.toDto(entry);
    }

    @Operation(summary = "Статистика: количество записей по уровням логирования")
    @GetMapping("/stats/levels")
    public Map<String, Long> countByLevel() {
        return logEntryService.countByLevel();
    }

    @Operation(summary = "Топ-N сообщений по частоте встречаемости")
    @GetMapping("/stats/top-messages")
    public Map<String, Long> topMessages(@RequestParam(defaultValue = "5") int limit) {
        return logEntryService.topFrequentMessages(limit);
    }

    @Operation(summary = "Группировка записей по уровню логирования")
    @GetMapping("/stats/groups")
    public Map<String, List<LogEntryDto>> groupByLevel() {
        Map<String, List<LogEntryDto>> result = new LinkedHashMap<>();
        logEntryService.groupByLevel().forEach((level, entries) -> result.put(level, mapper.toDtoList(entries)));
        return result;
    }

    private void checkPeriod(LocalDateTime from, LocalDateTime to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Начало периода не может быть позже его окончания");
        }
    }

    private String normalize(String level) {
        return requireText(level, "level").toUpperCase(Locale.ROOT);
    }

    private String requireText(String value, String paramName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Параметр '" + paramName + "' не может быть пустым");
        }
        return value.trim();
    }
}