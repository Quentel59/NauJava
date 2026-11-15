package ru.quentel.naujava.service;

import java.util.List;
import java.util.Map;

import ru.quentel.naujava.entity.LogEntry;

/**
 * Бизнес-логика анализа логов.
 */
public interface LogEntryService {

    /** Добавляет запись лога в указанный файл с указанным уровнем. */
    LogEntry addLogEntry(Long logFileId, String levelName, String message, Integer lineNumber);

    /** Ищет запись по идентификатору. */
    LogEntry findById(Long id);

    /** Возвращает все записи. */
    List<LogEntry> findAll();

    /** Меняет текст сообщения. */
    void updateMessage(Long id, String newMessage);

    /** Удаляет запись по идентификатору. */
    void deleteById(Long id);

    /** Статистика: количество записей по уровням логирования. */
    Map<String, Long> countByLevel();

    /** Топ-N сообщений по частоте встречаемости. */
    Map<String, Long> topFrequentMessages(int limit);

    /** Группировка записей по уровню логирования. */
    Map<String, List<LogEntry>> groupByLevel();
}