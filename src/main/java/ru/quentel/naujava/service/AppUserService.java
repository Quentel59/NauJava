package ru.quentel.naujava.service;

import java.util.List;

import ru.quentel.naujava.entity.AppUser;
import ru.quentel.naujava.entity.UserRole;

public interface AppUserService {

    /** Добавляет пользователя с указанной ролью, пароль хешируется. */
    AppUser addUser(String username, String rawPassword, String email, UserRole role);

    /** Самостоятельная регистрация — всегда роль USER. */
    AppUser registerUser(String username, String rawPassword, String email);

    /** Получает пользователя по имени. */
    AppUser findByUsername(String username);

    /** Все пользователи. */
    List<AppUser> findAll();

    /** Количество зарегистрированных пользователей (пригодится в лабе 7). */
    long countUsers();
}