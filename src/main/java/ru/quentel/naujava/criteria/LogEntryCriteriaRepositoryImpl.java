package ru.quentel.naujava.criteria;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import ru.quentel.naujava.entity.Application;
import ru.quentel.naujava.entity.LogEntry;
import ru.quentel.naujava.entity.LogFile;
import ru.quentel.naujava.entity.LogLevel;

@Repository
@Transactional(readOnly = true)
public class LogEntryCriteriaRepositoryImpl implements LogEntryCriteriaRepository {

    private final EntityManager entityManager;

    @Autowired
    public LogEntryCriteriaRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<LogEntry> findByLevelNameAndEventTimeBetween(String levelName, LocalDateTime from, LocalDateTime to) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LogEntry> cq = cb.createQuery(LogEntry.class);
        Root<LogEntry> entry = cq.from(LogEntry.class);

        Join<LogEntry, LogLevel> level = entry.join("level", JoinType.INNER);

        Predicate levelPredicate = cb.equal(level.get("name"), levelName);
        Predicate periodPredicate = cb.between(entry.<LocalDateTime>get("eventTime"), from, to);

        cq.select(entry)
                .where(cb.and(levelPredicate, periodPredicate))
                .orderBy(cb.asc(entry.get("eventTime")));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<LogEntry> findByApplicationName(String applicationName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LogEntry> cq = cb.createQuery(LogEntry.class);
        Root<LogEntry> entry = cq.from(LogEntry.class);

        Join<LogEntry, LogFile> file = entry.join("logFile", JoinType.INNER);
        Join<LogFile, Application> application = file.join("application", JoinType.INNER);

        cq.select(entry)
                .where(cb.equal(application.get("name"), applicationName))
                .orderBy(cb.asc(entry.get("eventTime")));

        return entityManager.createQuery(cq).getResultList();
    }
}