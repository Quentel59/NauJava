package ru.quentel.naujava.console;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.quentel.naujava.entity.LogEntry;
import ru.quentel.naujava.service.LogEntryService;

/**
 * Разбор и обработка консольных команд.
 */
@Component
public class CommandProcessor {

    private final LogEntryService logEntryService;

    @Autowired
    public CommandProcessor(LogEntryService logEntryService) {
        this.logEntryService = logEntryService;
    }

    public void processCommand(String input) {
        String[] parts = input.trim().split("\\s+", 3);
        String command = parts[0].toLowerCase(Locale.ROOT);
        try {
            switch (command) {
                case "add" -> {
                    checkArgs(parts, 3, "add <LEVEL> <сообщение>");
                    LogEntry entry = logEntryService.addLogEntry(parts[1], parts[2]);
                    System.out.println("Добавлено: " + entry);
                }
                case "find" -> {
                    checkArgs(parts, 2, "find <id>");
                    System.out.println(logEntryService.findById(parseId(parts[1])));
                }
                case "update" -> {
                    checkArgs(parts, 3, "update <id> <новое сообщение>");
                    logEntryService.updateMessage(parseId(parts[1]), parts[2]);
                    System.out.println("Сообщение обновлено");
                }
                case "delete" -> {
                    checkArgs(parts, 2, "delete <id>");
                    logEntryService.deleteById(parseId(parts[1]));
                    System.out.println("Запись удалена");
                }
                case "list" -> {
                    List<LogEntry> all = logEntryService.findAll();
                    all.forEach(System.out::println);
                    System.out.println("Всего записей: " + all.size());
                }
                case "stats" -> {
                    Map<String, Long> stats = logEntryService.countByLevel();
                    if (stats.isEmpty()) {
                        System.out.println("Нет данных для статистики");
                    } else {
                        System.out.println("Количество записей по уровням:");
                        stats.forEach((level, count) -> System.out.printf("  %-6s %d%n", level, count));
                    }
                }
                case "top" -> {
                    int limit = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 5;
                    Map<String, Long> top = logEntryService.topFrequentMessages(limit);
                    if (top.isEmpty()) {
                        System.out.println("Нет данных для топа сообщений");
                    } else {
                        System.out.println("Топ-" + limit + " сообщений по частоте:");
                        top.forEach((message, count) -> System.out.printf("  %4d x %s%n", count, message));
                    }
                }
                case "group" -> {
                    Map<String, List<LogEntry>> groups = logEntryService.groupByLevel();
                    if (groups.isEmpty()) {
                        System.out.println("Хранилище пусто");
                    } else {
                        groups.forEach((level, entries) -> {
                            System.out.println("Уровень " + level + " (" + entries.size() + "):");
                            entries.forEach(entry -> System.out.println("  " + entry));
                        });
                    }
                }
                case "help" -> printHelp();
                default -> System.out.println("Неизвестная команда: " + command + ". Введите 'help'.");
            }
        } catch (NumberFormatException ex) {
            System.out.println("Ошибка: ожидалось целое число. " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println("Ошибка: " + ex.getMessage());
        }
    }

    private static void checkArgs(String[] parts, int required, String usage) {
        if (parts.length < required) {
            throw new IllegalArgumentException("недостаточно аргументов. Формат: " + usage);
        }
    }

    private static Long parseId(String value) {
        return Long.valueOf(value.trim());
    }

    private void printHelp() {
        System.out.println("""
                Доступные команды:
                  add <LEVEL> <сообщение>   добавить запись (LEVEL: TRACE, DEBUG, INFO, WARN, ERROR, FATAL)
                  find <id>                 найти запись по id
                  update <id> <сообщение>   изменить текст сообщения
                  delete <id>               удалить запись
                  list                      показать все записи
                  stats                     статистика по уровням логирования
                  top [N]                   топ-N сообщений по частоте встречаемости (по умолчанию 5)
                  group                     сгруппировать записи по уровню
                  help                      эта справка
                  exit                      выход из консольного режима
                """);
    }
}