package ru.quentel.naujava.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import ru.quentel.naujava.entity.AppUser;

/**
 * Репозиторий пользователей.
 * Намеренно БЕЗ @RepositoryRestResource: при detection-strategy=annotated
 * ресурс не экспортируется наружу — пароли не должны утечь через REST.
 * Работа с пользователями идёт через AppUserRestController и форму регистрации.
 */
public interface AppUserRepository extends CrudRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    @Override
    List<AppUser> findAll();
}