package ru.quentel.naujava.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ru.quentel.naujava.entity.LogFile;

public interface LogFileRepository extends CrudRepository<LogFile, Long> {

    /** Все файлы логов приложения. */
    List<LogFile> findByApplicationId(Long applicationId);
}