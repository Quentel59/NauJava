package ru.quentel.naujava.criteria;

import java.time.LocalDateTime;
import java.util.List;

import ru.quentel.naujava.entity.LogEntry;

/**
 * Кастомные запросы к записям лога на Criteria API.
 * Аналог методов из LogEntryRepository.
 */
public interface LogEntryCriteriaRepository {

    /** Аналог findByLevelNameAndEventTimeBetween (And + Between). */
    List<LogEntry> findByLevelNameAndEventTimeBetween(String levelName, LocalDateTime from, LocalDateTime to);

    /** Аналог findByApplicationName (через связанные сущности LogFile -> Application). */
    List<LogEntry> findByApplicationName(String applicationName);
}