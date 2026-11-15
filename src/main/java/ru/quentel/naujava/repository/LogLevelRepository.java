package ru.quentel.naujava.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import ru.quentel.naujava.entity.LogLevel;

@RepositoryRestResource(path = "log-levels")
public interface LogLevelRepository extends CrudRepository<LogLevel, Long> {

    /** Поиск уровня логирования по названию. */
    Optional<LogLevel> findByName(String name);
}