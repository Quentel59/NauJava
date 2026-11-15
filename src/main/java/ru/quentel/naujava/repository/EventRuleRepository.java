package ru.quentel.naujava.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ru.quentel.naujava.entity.EventRule;

@RepositoryRestResource(path = "event-rules")
public interface EventRuleRepository extends CrudRepository<EventRule, Long> {

    /** Включённые правила приложения. */
    List<EventRule> findByApplicationIdAndEnabledTrue(Long applicationId);
}