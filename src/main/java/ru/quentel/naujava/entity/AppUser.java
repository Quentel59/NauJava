package ru.quentel.naujava.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Пользователь приложения. Пароль хранится только в виде хеша BCrypt.
 * Назван AppUser, чтобы не конфликтовать с org.springframework.security.core.userdetails.User.
 */
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Логин пользователя. */
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    /** Хеш пароля (BCrypt). */
    @Column(name = "password", nullable = false, length = 200)
    private String password;

    /** Электронная почта. */
    @Column(name = "email", length = 200)
    private String email;

    /** Роль в системе. */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    /** Дата регистрации. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "uploadedBy")
    private List<LogFile> uploadedFiles = new ArrayList<>();

    public AppUser() {
    }

    public AppUser(String username, String password, String email, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<LogFile> getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(List<LogFile> uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }

    @Override
    public String toString() {
        return "AppUser{id=" + id + ", username='" + username + "', role=" + role + "}";
    }
}