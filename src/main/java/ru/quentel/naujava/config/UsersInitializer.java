package ru.quentel.naujava.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ru.quentel.naujava.entity.AppUser;
import ru.quentel.naujava.entity.UserRole;
import ru.quentel.naujava.repository.AppUserRepository;

/**
 * Создаёт администратора и обычного пользователя стенда, если их ещё нет.
 * Пароли хранятся в виде BCrypt-хеша.
 */
@Component
public class UsersInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.admin.username:admin}")
    private String adminUsername;

    @Value("${app.security.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.security.user.username:user}")
    private String userUsername;

    @Value("${app.security.user.password:user123}")
    private String userPassword;

    @Autowired
    public UsersInitializer(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        createIfAbsent(adminUsername, adminPassword, "admin@example.com", UserRole.ADMIN);
        createIfAbsent(userUsername, userPassword, "user@example.com", UserRole.USER);
    }

    private void createIfAbsent(String username, String rawPassword, String email, UserRole role) {
        if (appUserRepository.existsByUsername(username)) {
            return;
        }
        AppUser user = new AppUser(username, passwordEncoder.encode(rawPassword), email, role);
        appUserRepository.save(user);
        System.out.println("Создан пользователь '" + username + "' с ролью " + role);
    }
}