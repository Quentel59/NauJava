package ru.quentel.naujava.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.quentel.naujava.entity.AppUser;
import ru.quentel.naujava.entity.UserRole;
import ru.quentel.naujava.exception.ResourceNotFoundException;
import ru.quentel.naujava.repository.AppUserRepository;

@Service
@Transactional(readOnly = true)
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AppUserServiceImpl(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AppUser addUser(String username, String rawPassword, String email, UserRole role) {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Имя пользователя должно содержать не менее 3 символов");
        }
        if (rawPassword == null || rawPassword.length() < 4) {
            throw new IllegalArgumentException("Пароль должен содержать не менее 4 символов");
        }
        if (email != null && !email.isBlank() && !email.contains("@")) {
            throw new IllegalArgumentException("Некорректный email");
        }
        String login = username.trim();
        if (appUserRepository.existsByUsername(login)) {
            throw new IllegalArgumentException("Пользователь '" + login + "' уже существует");
        }

        AppUser user = new AppUser(
                login,
                passwordEncoder.encode(rawPassword),
                email == null || email.isBlank() ? null : email.trim(),
                role == null ? UserRole.USER : role);
        return appUserRepository.save(user);
    }

    @Override
    @Transactional
    public AppUser registerUser(String username, String rawPassword, String email) {
        return addUser(username, rawPassword, email, UserRole.USER);
    }

    @Override
    public AppUser findByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        return appUserRepository.findByUsername(username.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь '" + username + "' не найден"));
    }

    @Override
    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }

    @Override
    public long countUsers() {
        return appUserRepository.count();
    }
}