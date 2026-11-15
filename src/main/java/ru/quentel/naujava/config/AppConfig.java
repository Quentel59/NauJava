package ru.quentel.naujava.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import ru.quentel.naujava.service.LogEntryService;

/**
 * Класс-конфигурация с конфигурационными свойствами приложения.
 * Сервис внедряется через конструктор, поэтому он гарантированно полностью
 * инициализирован до вызова метода с @PostConstruct.
 */
@Configuration
public class AppConfig {

    private final LogEntryService logEntryService;

    @Value("${app.name}")
    private String appName;

    @Value("${app.version:0.0.1}")
    private String appVersion;

    @Autowired
    public AppConfig(LogEntryService logEntryService) {
        this.logEntryService = logEntryService;
    }

    @PostConstruct
    public void printApplicationInfo() {
        System.out.println("========================================");
        System.out.println("Приложение: " + appName);
        System.out.println("Версия:     " + appVersion);
        System.out.println("Тема:       анализ логов и журналов событий");
        System.out.println("Записей в хранилище: " + logEntryService.findAll().size());
        System.out.println("========================================");
    }

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }
}