package ru.quentel.naujava.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Стороннее приложение — источник логов.
 */
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Название приложения. */
    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;

    /** Производитель/вендор. */
    @Column(name = "vendor", length = 200)
    private String vendor;

    /** Описание приложения. */
    @Column(name = "description", length = 1000)
    private String description;

    @OneToMany(mappedBy = "application")
    private List<LogFile> logFiles = new ArrayList<>();

    public Application() {
    }

    public Application(String name, String vendor, String description) {
        this.name = name;
        this.vendor = vendor;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<LogFile> getLogFiles() {
        return logFiles;
    }

    public void setLogFiles(List<LogFile> logFiles) {
        this.logFiles = logFiles;
    }

    @Override
    public String toString() {
        return "Application{id=" + id + ", name='" + name + "', vendor='" + vendor + "'}";
    }
}