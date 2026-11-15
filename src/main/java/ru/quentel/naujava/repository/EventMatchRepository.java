package ru.quentel.naujava.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ru.quentel.naujava.entity.EventMatch;

public interface EventMatchRepository extends CrudRepository<EventMatch, Long> {

    /** Все совпадения правила. */
    List<EventMatch> findByRuleId(Long ruleId);

    /** Есть ли события, ссылающиеся на запись лога. */
    boolean existsByLogEntryId(Long logEntryId);
}