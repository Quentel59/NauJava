package ru.quentel.naujava.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.format.annotation.DateTimeFormat;

import ru.quentel.naujava.entity.LogEntry;

@RepositoryRestResource(path = "log-entries")
public interface LogEntryRepository extends CrudRepository<LogEntry, Long> {

    /** Все записи сразу со связанными уровнем, файлом и приложением. */
    @Override
    @EntityGraph(attributePaths = {"level", "logFile", "logFile.application"})
    List<LogEntry> findAll();

    @Override
    @EntityGraph(attributePaths = {"level", "logFile", "logFile.application"})
    Optional<LogEntry> findById(Long id);

    /**
     * МЕТОД 1 (Query Lookup Strategies: And + Between).
     * Записи заданного уровня логирования за период времени.
     */
    @EntityGraph(attributePaths = {"level", "logFile", "logFile.application"})
    List<LogEntry> findByLevelNameAndEventTimeBetween(
            @Param("levelName") String levelName,
            @Param("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Param("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to);

    /**
     * МЕТОД 2 (JPQL через связанные сущности LogFile -> Application).
     * Все записи логов приложения с заданным названием.
     */
    @EntityGraph(attributePaths = {"level", "logFile", "logFile.application"})
    @Query("SELECT e FROM LogEntry e JOIN e.logFile f JOIN f.application a "
            + "WHERE a.name = :applicationName ORDER BY e.eventTime")
    List<LogEntry> findByApplicationName(@Param("applicationName") String applicationName);

    /** Записи конкретного файла лога (для транзакционного удаления). */
    List<LogEntry> findByLogFileIdOrderByIdAsc(Long logFileId);
}