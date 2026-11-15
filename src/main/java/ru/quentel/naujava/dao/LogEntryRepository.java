package ru.quentel.naujava.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.quentel.naujava.entity.LogEntry;

/**
 * Слой доступа к данным
 */
@Component
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class LogEntryRepository implements CrudRepository<LogEntry, Long> {

    /** Имитация БД*/
    private final List<LogEntry> logContainer;

    /** Генератор уникальных идентификаторов. */
    private final AtomicLong idSequence = new AtomicLong(0);

    @Autowired
    public LogEntryRepository(@Qualifier("logContainer") List<LogEntry> logContainer) {
        this.logContainer = logContainer;
    }

    @Override
    public void create(LogEntry entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Сущность не может быть null");
        }
        if (entity.getId() == null) {
            entity.setId(idSequence.incrementAndGet());
        } else {
            if (read(entity.getId()) != null) {
                throw new IllegalArgumentException("Запись с id=" + entity.getId() + " уже существует");
            }
            idSequence.updateAndGet(current -> Math.max(current, entity.getId()));
        }
        logContainer.add(entity);
    }

    @Override
    public LogEntry read(Long id) {
        return logContainer.stream()
                .filter(entry -> Objects.equals(entry.getId(), id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void update(LogEntry entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("Для обновления нужен id сущности");
        }
        for (int i = 0; i < logContainer.size(); i++) {
            if (Objects.equals(logContainer.get(i).getId(), entity.getId())) {
                logContainer.set(i, entity);
                return;
            }
        }
        throw new IllegalArgumentException("Запись лога с id=" + entity.getId() + " не найдена");
    }

    @Override
    public void delete(Long id) {
        boolean removed = logContainer.removeIf(entry -> Objects.equals(entry.getId(), id));
        if (!removed) {
            throw new IllegalArgumentException("Запись лога с id=" + id + " не найдена");
        }
    }

    @Override
    public List<LogEntry> readAll() {
        return new ArrayList<>(logContainer);
    }
}