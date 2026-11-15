package ru.quentel.naujava.controller.rest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ru.quentel.naujava.dto.AppUserDto;
import ru.quentel.naujava.entity.UserRole;
import ru.quentel.naujava.service.AppUserService;

@RestController
@RequestMapping("/custom/users")
@Tag(name = "Пользователи", description = "Регистрация и просмотр пользователей системы")
public class AppUserRestController {

    private final AppUserService appUserService;

    @Autowired
    public AppUserRestController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Operation(summary = "Текущий авторизованный пользователь")
    @GetMapping("/me")
    public AppUserDto me(Authentication authentication) {
        return AppUserDto.from(appUserService.findByUsername(authentication.getName()));
    }

    @Operation(summary = "Пользователь по имени (только ADMIN)")
    @GetMapping("/by-username")
    public AppUserDto byUsername(@RequestParam String username) {
        return AppUserDto.from(appUserService.findByUsername(username));
    }

    @Operation(summary = "Количество зарегистрированных пользователей (только ADMIN)")
    @GetMapping("/count")
    public Map<String, Long> count() {
        return Map.of("usersCount", appUserService.countUsers());
    }

    @Operation(summary = "Список пользователей (только ADMIN)")
    @GetMapping
    public List<AppUserDto> all() {
        return appUserService.findAll().stream().map(AppUserDto::from).toList();
    }

    @Operation(summary = "Создать пользователя (только ADMIN)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppUserDto create(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam(required = false) String email,
                             @RequestParam(defaultValue = "USER") UserRole role) {
        return AppUserDto.from(appUserService.addUser(username, password, email, role));
    }
}