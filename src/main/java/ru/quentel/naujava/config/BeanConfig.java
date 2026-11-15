package ru.quentel.naujava.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import ru.quentel.naujava.console.CommandProcessor;
import ru.quentel.naujava.entity.LogEntry;

/**
 * Конфигурация бинов приложения.
 */
@Configuration
public class BeanConfig {

    /**
     * Имитация БД: единственный (singleton) список записей лога на всё приложение.
     */
    @Bean
    @Scope(value = BeanDefinition.SCOPE_SINGLETON)
    public List<LogEntry> logContainer() {
        return new ArrayList<>();
    }

    /**
     * Консольный ввод. Отключается свойством app.console.enabled=false
     * (пригодится для автотестов в следующих работах).
     */
    @Bean
    @ConditionalOnProperty(name = "app.console.enabled", havingValue = "true", matchIfMissing = true)
    public CommandLineRunner commandScanner(CommandProcessor commandProcessor) {
        return args -> {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("Введите команду ('help' — список команд, 'exit' — выход из консоли).");
                while (true) {
                    System.out.print("> ");
                    if (!scanner.hasNextLine()) {
                        break;
                    }
                    String input = scanner.nextLine().trim();
                    if (input.isEmpty()) {
                        continue;
                    }
                    if ("exit".equalsIgnoreCase(input)) {
                        System.out.println("Выход из консольного режима...");
                        break;
                    }
                    commandProcessor.processCommand(input);
                }
            }
        };
    }
}