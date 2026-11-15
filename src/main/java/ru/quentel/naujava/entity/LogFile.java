package ru.quentel.naujava.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Загруженный файл логов. Принадлежит приложению и содержит записи лога.
 * Каскадное удаление записей намеренно НЕ включено: целостность обеспечивается
 * транзакционным методом LogFileService.deleteLogFileWithEntries().
 */
@Entity
@Table(name = "log_files")
public class LogFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Имя файла. */
    @Column(name = "file_name", nullable = false, length = 300)
    private String fileName;

    /** Путь хранения файла. */
    @Column(name = "storage_path", length = 1000)
    private String storagePath;

    /** Размер файла в байтах. */
    @Column(name = "size_bytes")
    private Long sizeBytes;

    /** Дата и время загрузки. */
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;
    /** Пользователь, загрузивший файл (необязательное поле). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id")
    private AppUser uploadedBy;

    @OneToMany(mappedBy = "logFile")
    private List<LogEntry> entries = new ArrayList<>();

    public LogFile() {
    }

    public LogFile(String fileName, String storagePath, Long sizeBytes, Application application) {
        this.fileName = fileName;
        this.storagePath = storagePath;
        this.sizeBytes = sizeBytes;
        this.application = application;
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public List<LogEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<LogEntry> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return "LogFile{id=" + id + ", fileName='" + fileName + "', sizeBytes=" + sizeBytes + "}";
    }
    public AppUser getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(AppUser uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

}

