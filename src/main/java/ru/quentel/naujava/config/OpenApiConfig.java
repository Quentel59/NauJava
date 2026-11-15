package ru.quentel.naujava.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Bean
    public OpenAPI logAnalyzerOpenApi() {
        return new OpenAPI().info(new Info()
                .title(appName + " REST API")
                .version(appVersion)
                .description("Анализ логов и журналов событий: загрузка логов сторонних приложений, "
                        + "поиск, группировка и статистика по частоте встречаемости. "
                        + "Разделы: /api/** — Spring Data REST, /custom/** — контроллеры вручную.")
                .contact(new Contact().name("Quentel59")));
    }
}