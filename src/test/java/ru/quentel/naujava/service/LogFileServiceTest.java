package ru.quentel.naujava.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ru.quentel.naujava.entity.Application;
import ru.quentel.naujava.entity.EventMatch;
import ru.quentel.naujava.entity.EventRule;
import ru.quentel.naujava.entity.LogEntry;
import ru.quentel.naujava.entity.LogFile;
import ru.quentel.naujava.entity.LogLevel;
import ru.quentel.naujava.repository.ApplicationRepository;
import ru.quentel.naujava.repository.EventMatchRepository;
import ru.quentel.naujava.repository.EventRuleRepository;
import ru.quentel.naujava.repository.LogEntryRepository;
import ru.quentel.naujava.repository.LogFileRepository;
import ru.quentel.naujava.repository.LogLevelRepository;

/**
 * Автотесты транзакционного метода удаления файла логов вместе с записями.
 */
@SpringBootTest
@ActiveProfiles("test")
class LogFileServiceTest {

    private final LogFileService logFileService;
    private final LogFileRepository logFileRepository;
    private final LogEntryRepository logEntryRepository;
    private final ApplicationRepository applicationRepository;
    private final LogLevelRepository logLevelRepository;
    private final EventRuleRepository eventRuleRepository;
    private final EventMatchRepository eventMatchRepository;

    @Autowired
    LogFileServiceTest(LogFileService logFileService,
                       LogFileRepository logFileRepository,
                       LogEntryRepository logEntryRepository,
                       ApplicationRepository applicationRepository,
                       LogLevelRepository logLevelRepository,
                       EventRuleRepository eventRuleRepository,
                       EventMatchRepository eventMatchRepository) {
        this.logFileService = logFileService;
        this.logFileRepository = logFileRepository;
        this.logEntryRepository = logEntryRepository;
        this.applicationRepository = applicationRepository;
        this.logLevelRepository = logLevelRepository;
        this.eventRuleRepository = eventRuleRepository;
        this.eventMatchRepository = eventMatchRepository;
    }

    private Application newApplication() {
        return applicationRepository.save(new Application(
                "tx-app-" + UUID.randomUUID(), "Test Vendor", "Приложение для теста транзакций"));
    }

    private LogEntry newEntry(LogFile logFile, String message) {
        LogLevel level = logLevelRepository.findByName("ERROR")
                .orElseGet(() -> logLevelRepository.save(new LogLevel("ERROR", 5, "Ошибки")));
        LogEntry entry = new LogEntry();
        entry.setLogFile(logFile);
        entry.setLevel(level);
        entry.setMessage(message);
        entry.setLineNumber(1);
        entry.setEventTime(LocalDateTime.now());
        entry.setRawLine(message);
        return logEntryRepository.save(entry);
    }

    /** Позитивный кейс: файл и все его записи удалены. */
    @Test
    void testDeleteLogFileWithEntries() {
        Application application = newApplication();
        LogFile logFile = logFileService.registerLogFile(application.getId(), "tx-ok.log", "/tmp/tx-ok.log", 100L);
        LogEntry first = newEntry(logFile, "Connection timeout");
        LogEntry second = newEntry(logFile, "Read timed out");

        logFileService.deleteLogFileWithEntries(logFile.getId());

        Assertions.assertTrue(logFileRepository.findById(logFile.getId()).isEmpty());
        Assertions.assertTrue(logEntryRepository.findById(first.getId()).isEmpty());
        Assertions.assertTrue(logEntryRepository.findById(second.getId()).isEmpty());
    }

    /** Негативный кейс: запись используется в событиях -> откат, данные целы. */
    @Test
    void testDeleteLogFileRollback() {
        Application application = newApplication();
        LogFile logFile = logFileService.registerLogFile(application.getId(), "tx-fail.log", "/tmp/tx-fail.log", 200L);
        LogEntry deletable = newEntry(logFile, "Connection timeout");
        LogEntry protectedEntry = newEntry(logFile, "Fatal error occurred");

        EventRule rule = eventRuleRepository.save(new EventRule(
                "Правило " + UUID.randomUUID(), "Fatal error", "Критические события", true, application));
        EventMatch match = eventMatchRepository.save(new EventMatch(rule, protectedEntry, "Fatal error", 0.9));

        Assertions.assertThrows(IllegalStateException.class,
                () -> logFileService.deleteLogFileWithEntries(logFile.getId()));

        // транзакция откатилась: файл и ОБЕ записи остались в БД
        Assertions.assertTrue(logFileRepository.findById(logFile.getId()).isPresent());
        Assertions.assertTrue(logEntryRepository.findById(deletable.getId()).isPresent());
        Assertions.assertTrue(logEntryRepository.findById(protectedEntry.getId()).isPresent());
        Assertions.assertTrue(eventMatchRepository.findById(match.getId()).isPresent());
    }

    /** Негативный кейс: несуществующий файл. */
    @Test
    void testDeleteUnknownLogFile() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> logFileService.deleteLogFileWithEntries(-1L));
    }

    /** Негативный кейс: регистрация файла с пустым именем. */
    @Test
    void testRegisterLogFileWithBlankName() {
        Application application = newApplication();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> logFileService.registerLogFile(application.getId(), "   ", "/tmp/x.log", 10L));
    }
}