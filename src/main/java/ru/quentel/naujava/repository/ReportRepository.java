package ru.quentel.naujava.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ru.quentel.naujava.entity.Report;

@RepositoryRestResource(path = "reports")
public interface ReportRepository extends CrudRepository<Report, Long> {

    @Override
    List<Report> findAll();

    /** Список отчётов от новых к старым. */
    List<Report> findAllByOrderByIdDesc();
}