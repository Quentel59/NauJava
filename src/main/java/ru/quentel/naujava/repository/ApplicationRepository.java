package ru.quentel.naujava.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import ru.quentel.naujava.entity.Application;

@RepositoryRestResource(path = "applications")

public interface ApplicationRepository extends CrudRepository<Application, Long> {

    /** Поиск приложения по названию. */
    Optional<Application> findByName(String name);
}