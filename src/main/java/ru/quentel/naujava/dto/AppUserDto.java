package ru.quentel.naujava.dto;

import java.time.LocalDateTime;

import ru.quentel.naujava.entity.AppUser;

/**
 * Представление пользователя. Пароль наружу не отдаётся никогда.
 */
public class AppUserDto {

    private final Long id;
    private final String username;
    private final String email;
    private final String role;
    private final LocalDateTime createdAt;

    public AppUserDto(Long id, String username, String email, String role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static AppUserDto from(AppUser user) {
        return new AppUserDto(user.getId(), user.getUsername(), user.getEmail(),
                user.getRole().name(), user.getCreatedAt());
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}