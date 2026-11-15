package ru.quentel.naujava.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ru.quentel.naujava.entity.LogFile;

@RepositoryRestResource(path = "log-files")
public interface LogFileRepository extends CrudRepository<LogFile, Long> {

    /** Все файлы логов приложения. */
    List<LogFile> findByApplicationId(Long applicationId);
}