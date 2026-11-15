package ru.quentel.naujava.service;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import ru.quentel.naujava.entity.AppUser;
import ru.quentel.naujava.entity.UserRole;
import ru.quentel.naujava.exception.ResourceNotFoundException;

@SpringBootTest
@ActiveProfiles("test")
class AppUserServiceTest {

    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    AppUserServiceTest(AppUserService appUserService, PasswordEncoder passwordEncoder) {
        this.appUserService = appUserService;
        this.passwordEncoder = passwordEncoder;
    }

    private String randomLogin() {
        return "user-" + UUID.randomUUID();
    }

    /** Позитивный кейс: регистрация, роль USER и хеширование пароля. */
    @Test
    void testRegisterUserEncodesPassword() {
        String login = randomLogin();
        String rawPassword = "secret123";

        AppUser saved = appUserService.registerUser(login, rawPassword, "test@example.com");
        AppUser found = appUserService.findByUsername(login);

        Assertions.assertEquals(UserRole.USER, found.getRole());
        Assertions.assertNotEquals(rawPassword, found.getPassword());
        Assertions.assertTrue(found.getPassword().startsWith("$2"));
        Assertions.assertTrue(passwordEncoder.matches(rawPassword, found.getPassword()));
        Assertions.assertEquals(saved.getId(), found.getId());
    }

    /** Негативный кейс: дубликат логина. */
    @Test
    void testDuplicateUsername() {
        String login = randomLogin();
        appUserService.registerUser(login, "secret123", null);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> appUserService.registerUser(login, "another123", null));
    }

    /** Негативный кейс: слишком короткий пароль. */
    @Test
    void testShortPassword() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> appUserService.registerUser(randomLogin(), "123", null));
    }

    /** Негативный кейс: пользователь не найден. */
    @Test
    void testFindUnknownUser() {
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> appUserService.findByUsername("absent-" + UUID.randomUUID()));
    }

    /** Позитивный кейс: администратор создаётся с ролью ADMIN. */
    @Test
    void testAddAdmin() {
        String login = randomLogin();
        AppUser admin = appUserService.addUser(login, "admin123", null, UserRole.ADMIN);

        Assertions.assertEquals(UserRole.ADMIN, admin.getRole());
        Assertions.assertTrue(appUserService.countUsers() >= 1);
    }
}