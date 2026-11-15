package ru.quentel.naujava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Кодировщик паролей: в БД хранится только BCrypt-хеш. */
    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // доступно всем
                        .requestMatchers("/registration", "/login", "/logout", "/error", "/css/**", "/js/**").permitAll()
                        // Swagger и мониторинг — только ADMIN
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                                "/swagger-resources/**", "/actuator/**").hasRole("ADMIN")
                        // управление пользователями — только ADMIN
                        .requestMatchers(HttpMethod.GET, "/custom/users").hasRole("ADMIN")
                        .requestMatchers("/custom/users/by-username", "/custom/users/count").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/custom/users").hasRole("ADMIN")
                        // всё остальное — только авторизованным
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/log-entries/view/list", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                // CSRF оставлен для HTML-форм; для REST-клиентов отключён (нужно в лабе 8)
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/custom/**"));

        return http.build();
    }
}