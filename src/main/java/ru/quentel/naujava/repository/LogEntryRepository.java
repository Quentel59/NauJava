package ru.quentel.naujava.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import ru.quentel.naujava.entity.LogEntry;

public interface LogEntryRepository extends CrudRepository<LogEntry, Long> {

    /** Переопределение ради удобного типа возвращаемого значения. */
    @Override
    List<LogEntry> findAll();

    /**
     * МЕТОД 1 (Query Lookup Strategies, ключевые слова And + Between).
     * Записи заданного уровня логирования за период времени.
     *
     * @param levelName название уровня (связанная сущность LogLevel)
     * @param from      начало периода
     * @param to        конец периода
     */
    List<LogEntry> findByLevelNameAndEventTimeBetween(String levelName, LocalDateTime from, LocalDateTime to);

    /**
     * МЕТОД 2 (JPQL через связанные сущности LogFile -> Application).
     * Все записи логов, принадлежащие приложению с заданным названием.
     *
     * @param applicationName название приложения
     */
    @Query("SELECT e FROM LogEntry e JOIN e.logFile f JOIN f.application a "
            + "WHERE a.name = :applicationName ORDER BY e.eventTime")
    List<LogEntry> findByApplicationName(@Param("applicationName") String applicationName);

    /** Записи конкретного файла лога (для транзакционного удаления). */
    List<LogEntry> findByLogFileIdOrderByIdAsc(Long logFileId);
}