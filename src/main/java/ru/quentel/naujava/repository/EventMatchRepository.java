package ru.quentel.naujava.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ru.quentel.naujava.entity.EventMatch;

@RepositoryRestResource(path = "event-matches")
public interface EventMatchRepository extends CrudRepository<EventMatch, Long> {

    /** Все совпадения правила. */
    List<EventMatch> findByRuleId(Long ruleId);

    /** Есть ли события, ссылающиеся на запись лога. */
    boolean existsByLogEntryId(Long logEntryId);
}