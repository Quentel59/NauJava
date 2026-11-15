package ru.quentel.naujava.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import jakarta.persistence.EntityManager;
import ru.quentel.naujava.entity.Application;
import ru.quentel.naujava.entity.LogEntry;
import ru.quentel.naujava.entity.LogFile;
import ru.quentel.naujava.repository.ApplicationRepository;
import ru.quentel.naujava.repository.EventMatchRepository;
import ru.quentel.naujava.repository.LogEntryRepository;
import ru.quentel.naujava.repository.LogFileRepository;

@Service
public class LogFileServiceImpl implements LogFileService {

    private final LogFileRepository logFileRepository;
    private final LogEntryRepository logEntryRepository;
    private final EventMatchRepository eventMatchRepository;
    private final ApplicationRepository applicationRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManager entityManager;

    @Autowired
    public LogFileServiceImpl(LogFileRepository logFileRepository,
                              LogEntryRepository logEntryRepository,
                              EventMatchRepository eventMatchRepository,
                              ApplicationRepository applicationRepository,
                              PlatformTransactionManager transactionManager,
                              EntityManager entityManager) {
        this.logFileRepository = logFileRepository;
        this.logEntryRepository = logEntryRepository;
        this.eventMatchRepository = eventMatchRepository;
        this.applicationRepository = applicationRepository;
        this.transactionManager = transactionManager;
        this.entityManager = entityManager;
    }

    @Override
    public LogFile registerLogFile(Long applicationId, String fileName, String storagePath, Long sizeBytes) {
        if (applicationId == null) {
            throw new IllegalArgumentException("Не указан идентификатор приложения");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Имя файла логов не может быть пустым");
        }
        if (sizeBytes != null && sizeBytes < 0) {
            throw new IllegalArgumentException("Размер файла не может быть отрицательным");
        }
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Приложение с id=" + applicationId + " не найдено"));

        LogFile logFile = new LogFile(fileName.trim(), storagePath, sizeBytes, application);
        return logFileRepository.save(logFile);
    }

    @Override
    public void deleteLogFileWithEntries(Long logFileId) {
        // ручное управление транзакцией через Transaction Manager
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            LogFile logFile = logFileRepository.findById(logFileId)
                    .orElseThrow(() -> new IllegalArgumentException("Файл логов с id=" + logFileId + " не найден"));

            List<LogEntry> entries = logEntryRepository.findByLogFileIdOrderByIdAsc(logFileId);
            for (LogEntry entry : entries) {
                if (eventMatchRepository.existsByLogEntryId(entry.getId())) {
                    throw new IllegalStateException("Запись лога id=" + entry.getId()
                            + " используется в событиях, удаление файла логов запрещено");
                }
                logEntryRepository.delete(entry);
                // отправляем DELETE в БД, чтобы откат транзакции был реальным
                entityManager.flush();
            }

            logFileRepository.delete(logFile);
            transactionManager.commit(status);
        } catch (RuntimeException ex) {
            transactionManager.rollback(status);
            throw ex;
        }
    }
}