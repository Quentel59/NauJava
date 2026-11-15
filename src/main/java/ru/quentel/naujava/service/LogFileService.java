package ru.quentel.naujava.service;

import ru.quentel.naujava.entity.LogFile;

public interface LogFileService {

    /** Регистрирует загруженный файл логов для приложения. */
    LogFile registerLogFile(Long applicationId, String fileName, String storagePath, Long sizeBytes);

    /**
     * Удаляет файл логов вместе со всеми его записями в одной транзакции.
     * Если удаление невозможно — все изменения откатываются.
     */
    void deleteLogFileWithEntries(Long logFileId);
}