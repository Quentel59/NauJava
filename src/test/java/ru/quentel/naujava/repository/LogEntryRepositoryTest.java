package ru.quentel.naujava.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ru.quentel.naujava.criteria.LogEntryCriteriaRepository;
import ru.quentel.naujava.entity.Application;
import ru.quentel.naujava.entity.LogEntry;
import ru.quentel.naujava.entity.LogFile;
import ru.quentel.naujava.entity.LogLevel;

/**
 * Автотесты кастомных методов репозитория (Query Method + JPQL)
 * и аналогичных методов на Criteria API.
 */
@SpringBootTest
@ActiveProfiles("test")
class LogEntryRepositoryTest {

    private final LogEntryRepository logEntryRepository;
    private final LogEntryCriteriaRepository criteriaRepository;
    private final ApplicationRepository applicationRepository;
    private final LogFileRepository logFileRepository;
    private final LogLevelRepository logLevelRepository;

    private Application application;
    private LogFile logFile;
    private LogLevel level;
    private LocalDateTime base;
    private LogEntry firstInRange;
    private LogEntry secondInRange;
    private LogEntry outOfRange;
    private int lineNumber = 0;

    @Autowired
    LogEntryRepositoryTest(LogEntryRepository logEntryRepository,
                           LogEntryCriteriaRepository criteriaRepository,
                           ApplicationRepository applicationRepository,
                           LogFileRepository logFileRepository,
                           LogLevelRepository logLevelRepository) {
        this.logEntryRepository = logEntryRepository;
        this.criteriaRepository = criteriaRepository;
        this.applicationRepository = applicationRepository;
        this.logFileRepository = logFileRepository;
        this.logLevelRepository = logLevelRepository;
    }

    @BeforeEach
    void setUp() {
        application = applicationRepository.save(new Application(
                "test-app-" + UUID.randomUUID(), "Test Vendor", "Приложение для автотестов"));
        logFile = logFileRepository.save(new LogFile(
                "test-" + UUID.randomUUID() + ".log", "/tmp/test.log", 1024L, application));
        level = logLevelRepository.findByName("INFO")
                .orElseGet(() -> logLevelRepository.save(new LogLevel("INFO", 3, "Информационные сообщения")));

        base = LocalDateTime.now().minusDays(1).withNano(0);
        firstInRange = saveEntry("Connection timeout", base);
        secondInRange = saveEntry("Connection pool is almost full", base.plusHours(1));
        outOfRange = saveEntry("Out of memory", base.plusHours(5));
    }

    private LogEntry saveEntry(String message, LocalDateTime eventTime) {
        LogEntry entry = new LogEntry();
        entry.setLogFile(logFile);
        entry.setLevel(level);
        entry.setMessage(message);
        entry.setLineNumber(++lineNumber);
        entry.setEventTime(eventTime);
        entry.setRawLine(eventTime + " INFO " + message);
        return logEntryRepository.save(entry);
    }

    private static List<Long> ids(List<LogEntry> entries) {
        return entries.stream().map(LogEntry::getId).sorted().toList();
    }

    /** Позитивный кейс: Query Method с And + Between находит записи внутри периода. */
    @Test
    void testFindByLevelNameAndEventTimeBetween() {
        List<Long> found = ids(logEntryRepository.findByLevelNameAndEventTimeBetween(
                level.getName(), base.minusMinutes(1), base.plusHours(2)));

        Assertions.assertTrue(found.contains(firstInRange.getId()));
        Assertions.assertTrue(found.contains(secondInRange.getId()));
        Assertions.assertFalse(found.contains(outOfRange.getId()));
    }

    /** Негативный кейс: вне периода наши записи не находятся. */
    @Test
    void testFindByLevelNameAndEventTimeBetweenNoMatches() {
        List<Long> found = ids(logEntryRepository.findByLevelNameAndEventTimeBetween(
                level.getName(), base.plusHours(10), base.plusHours(12)));

        Assertions.assertFalse(found.contains(firstInRange.getId()));
        Assertions.assertFalse(found.contains(secondInRange.getId()));
        Assertions.assertFalse(found.contains(outOfRange.getId()));
    }

    /** Позитивный кейс: JPQL через LogFile -> Application. */
    @Test
    void testFindByApplicationName() {
        List<Long> found = ids(logEntryRepository.findByApplicationName(application.getName()));

        Assertions.assertEquals(3, found.size());
        Assertions.assertTrue(found.containsAll(
                List.of(firstInRange.getId(), secondInRange.getId(), outOfRange.getId())));
    }

    /** Негативный кейс: несуществующее приложение -> пустой результат. */
    @Test
    void testFindByUnknownApplicationName() {
        List<LogEntry> found = logEntryRepository.findByApplicationName("absent-" + UUID.randomUUID());
        Assertions.assertTrue(found.isEmpty());
    }

    /** Criteria API возвращает тот же результат, что и Query Method. */
    @Test
    void testCriteriaBetweenEqualsQueryMethod() {
        LocalDateTime from = base.minusMinutes(1);
        LocalDateTime to = base.plusHours(2);

        List<Long> viaQueryMethod = ids(logEntryRepository.findByLevelNameAndEventTimeBetween(level.getName(), from, to));
        List<Long> viaCriteria = ids(criteriaRepository.findByLevelNameAndEventTimeBetween(level.getName(), from, to));

        Assertions.assertFalse(viaCriteria.isEmpty());
        Assertions.assertEquals(viaQueryMethod, viaCriteria);
    }

    /** Criteria API возвращает тот же результат, что и JPQL-запрос. */
    @Test
    void testCriteriaFindByApplicationNameEqualsJpql() {
        List<Long> viaJpql = ids(logEntryRepository.findByApplicationName(application.getName()));
        List<Long> viaCriteria = ids(criteriaRepository.findByApplicationName(application.getName()));

        Assertions.assertEquals(3, viaCriteria.size());
        Assertions.assertEquals(viaJpql, viaCriteria);
    }
}