package ru.quentel.naujava.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ru.quentel.naujava.entity.EventRule;

public interface EventRuleRepository extends CrudRepository<EventRule, Long> {

    /** Включённые правила приложения. */
    List<EventRule> findByApplicationIdAndEnabledTrue(Long applicationId);
}