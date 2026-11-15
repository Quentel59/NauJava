package ru.quentel.naujava.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.quentel.naujava.entity.LogEntry;
import ru.quentel.naujava.entity.LogFile;
import ru.quentel.naujava.entity.LogLevel;
import ru.quentel.naujava.exception.ResourceNotFoundException;
import ru.quentel.naujava.repository.LogEntryRepository;
import ru.quentel.naujava.repository.LogFileRepository;
import ru.quentel.naujava.repository.LogLevelRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты LogEntryServiceImpl с использованием Mockito.
 * Не поднимают Spring-контекст, работают с моками репозиториев.
 */
@ExtendWith(MockitoExtension.class)
class LogEntryServiceMockTest {

    @Mock
    private LogEntryRepository logEntryRepository;

    @Mock
    private LogFileRepository logFileRepository;

    @Mock
    private LogLevelRepository logLevelRepository;

    @InjectMocks
    private LogEntryServiceImpl logEntryService;

    /** Позитивный кейс: добавление записи лога. */
    @Test
    void testAddLogEntrySuccess() {
        Long logFileId = 1L;
        String levelName = "ERROR";
        String message = "Connection timeout";
        Integer lineNumber = 10;

        LogFile logFile = new LogFile();
        logFile.setId(logFileId);
        logFile.setFileName("test.log");

        LogLevel level = new LogLevel();
        level.setId(5L);
        level.setName(levelName);

        LogEntry savedEntry = new LogEntry();
        savedEntry.setId(100L);
        savedEntry.setLogFile(logFile);
        savedEntry.setLevel(level);
        savedEntry.setMessage(message);
        savedEntry.setLineNumber(lineNumber);
        savedEntry.setEventTime(LocalDateTime.now());

        when(logFileRepository.findById(logFileId)).thenReturn(Optional.of(logFile));
        when(logLevelRepository.findByName(levelName)).thenReturn(Optional.of(level));
        when(logEntryRepository.save(any(LogEntry.class))).thenReturn(savedEntry);

        LogEntry result = logEntryService.addLogEntry(logFileId, levelName, message, lineNumber);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(100L, result.getId());
        Assertions.assertEquals(message, result.getMessage());
        verify(logFileRepository).findById(logFileId);
        verify(logLevelRepository).findByName(levelName);
        verify(logEntryRepository).save(any(LogEntry.class));
    }

    /** Негативный кейс: файл логов не найден. */
    @Test
    void testAddLogEntryLogFileNotFound() {
        Long logFileId = 999L;
        when(logFileRepository.findById(logFileId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> logEntryService.addLogEntry(logFileId, "ERROR", "test", 1));

        Assertions.assertTrue(ex.getMessage().contains("не найден"));
        verify(logEntryRepository, never()).save(any());
    }

    /** Негативный кейс: уровень логирования не найден в справочнике. */
    @Test
    void testAddLogEntryLevelNotFound() {
        Long logFileId = 1L;
        LogFile logFile = new LogFile();
        logFile.setId(logFileId);

        when(logFileRepository.findById(logFileId)).thenReturn(Optional.of(logFile));
        when(logLevelRepository.findByName("UNKNOWN")).thenReturn(Optional.empty());

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> logEntryService.addLogEntry(logFileId, "UNKNOWN", "test", 1));

        Assertions.assertTrue(ex.getMessage().contains("отсутствует в справочнике"));
        verify(logEntryRepository, never()).save(any());
    }

    /** Негативный кейс: пустое сообщение. */
    @Test
    void testAddLogEntryBlankMessage() {
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> logEntryService.addLogEntry(1L, "ERROR", "   ", 1));

        Assertions.assertTrue(ex.getMessage().contains("не может быть пустым"));
        verify(logEntryRepository, never()).save(any());
    }

    /** Позитивный кейс: поиск записи по id. */
    @Test
    void testFindByIdSuccess() {
        Long entryId = 100L;
        LogEntry entry = new LogEntry();
        entry.setId(entryId);
        entry.setMessage("test");

        when(logEntryRepository.findById(entryId)).thenReturn(Optional.of(entry));

        LogEntry result = logEntryService.findById(entryId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(entryId, result.getId());
    }

    /** Негативный кейс: запись не найдена. */
    @Test
    void testFindByIdNotFound() {
        Long entryId = 999L;
        when(logEntryRepository.findById(entryId)).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> logEntryService.findById(entryId));
    }

    /** Позитивный кейс: обновление сообщения. */
    @Test
    void testUpdateMessageSuccess() {
        Long entryId = 100L;
        String newMessage = "Updated message";

        LogEntry entry = new LogEntry();
        entry.setId(entryId);
        entry.setMessage("Old message");

        when(logEntryRepository.findById(entryId)).thenReturn(Optional.of(entry));
        when(logEntryRepository.save(any(LogEntry.class))).thenReturn(entry);

        logEntryService.updateMessage(entryId, newMessage);

        Assertions.assertEquals(newMessage, entry.getMessage());
        verify(logEntryRepository).save(entry);
    }

    /** Негативный кейс: удаление несуществующей записи. */
    @Test
    void testDeleteByIdNotFound() {
        Long entryId = 999L;
        when(logEntryRepository.findById(entryId)).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> logEntryService.deleteById(entryId));
    }

    /** Позитивный кейс: получение всех записей. */
    @Test
    void testFindAll() {
        LogEntry entry1 = new LogEntry();
        entry1.setId(1L);
        LogEntry entry2 = new LogEntry();
        entry2.setId(2L);

        when(logEntryRepository.findAll()).thenReturn(List.of(entry1, entry2));

        List<LogEntry> result = logEntryService.findAll();

        Assertions.assertEquals(2, result.size());
        verify(logEntryRepository).findAll();
    }
}