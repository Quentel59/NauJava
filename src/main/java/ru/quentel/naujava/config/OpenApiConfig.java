package ru.quentel.naujava.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * Описание API для Swagger UI.
 * Эндпоинты /custom/** springdoc находит сам по аннотациям контроллеров,
 * а CRUD-эндпоинты Spring Data REST (/api/**) описаны здесь явно.
 */
@Configuration
public class OpenApiConfig {

    private static final String DATA_REST_TAG = "Spring Data REST";
    private static final String HAL = "application/hal+json";

    /** Ресурсы Spring Data REST: путь -> описание. */
    private static final String[][] RESOURCES = {
            {"applications", "приложения — источники логов"},
            {"log-files", "файлы логов"},
            {"log-levels", "уровни логирования"},
            {"log-entries", "записи логов"},
            {"event-rules", "правила поиска событий"},
            {"event-matches", "совпадения правил с записями логов"},
            {"reports", "отчёты со статистикой приложения"}
    };

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Bean
    public OpenAPI logAnalyzerOpenApi() {
        OpenAPI openApi = new OpenAPI()
                .info(new Info()
                        .title(appName + " REST API")
                        .version(appVersion)
                        .description("Анализ логов и журналов событий. "
                                + "/api/** — CRUD-эндпоинты Spring Data REST, "
                                + "/custom/** — контроллеры, реализованные вручную.")
                        .contact(new Contact().name("Quentel59")))
                .addTagsItem(new Tag().name(DATA_REST_TAG)
                        .description("Эндпоинты, автоматически созданные Spring Data REST из репозиториев"));

        for (String[] resource : RESOURCES) {
            addCollectionPath(openApi, resource[0], resource[1]);
            addItemPath(openApi, resource[0], resource[1]);
        }
        addSearchPaths(openApi);
        return openApi;
    }

    private void addCollectionPath(OpenAPI openApi, String path, String description) {
        openApi.path("/api/" + path, new PathItem()
                .get(operation("Список: " + description, "list_" + path)
                        .addParametersItem(queryParam("page", "integer", "Номер страницы (с 0)"))
                        .addParametersItem(queryParam("size", "integer", "Размер страницы"))
                        .addParametersItem(queryParam("sort", "string", "Сортировка, например eventTime,desc"))
                        .responses(new ApiResponses().addApiResponse("200", halResponse("Список ресурсов"))))
                .post(operation("Создать: " + description, "create_" + path)
                        .requestBody(new RequestBody().required(true).content(jsonBody()))
                        .responses(new ApiResponses()
                                .addApiResponse("201", new ApiResponse().description("Ресурс создан")))));
    }

    private void addItemPath(OpenAPI openApi, String path, String description) {
        Parameter id = new Parameter().name("id").in("path").required(true)
                .description("Идентификатор ресурса").schema(new Schema<>().type("integer"));

        openApi.path("/api/" + path + "/{id}", new PathItem()
                .get(operation("Получить: " + description, "get_" + path)
                        .addParametersItem(id)
                        .responses(new ApiResponses()
                                .addApiResponse("200", halResponse("Ресурс"))
                                .addApiResponse("404", new ApiResponse().description("Ресурс не найден"))))
                .put(operation("Заменить: " + description, "replace_" + path)
                        .addParametersItem(id)
                        .requestBody(new RequestBody().required(true).content(jsonBody()))
                        .responses(new ApiResponses().addApiResponse("200", halResponse("Обновлённый ресурс"))))
                .patch(operation("Частично обновить: " + description, "update_" + path)
                        .addParametersItem(id)
                        .requestBody(new RequestBody().required(true).content(jsonBody()))
                        .responses(new ApiResponses().addApiResponse("200", halResponse("Обновлённый ресурс"))))
                .delete(operation("Удалить: " + description, "delete_" + path)
                        .addParametersItem(id)
                        .responses(new ApiResponses()
                                .addApiResponse("204", new ApiResponse().description("Ресурс удалён")))));
    }

    /** Кастомные методы репозитория, доступные через Spring Data REST search. */
    private void addSearchPaths(OpenAPI openApi) {
        openApi.path("/api/log-entries/search/findByApplicationName", new PathItem()
                .get(operation("Записи логов приложения (JPQL через LogFile -> Application)", "search_byApplication")
                        .addParametersItem(queryParam("applicationName", "string", "Название приложения"))
                        .responses(new ApiResponses().addApiResponse("200", halResponse("Найденные записи")))));

        openApi.path("/api/log-entries/search/findByLevelNameAndEventTimeBetween", new PathItem()
                .get(operation("Записи заданного уровня за период (And + Between)", "search_byLevelAndPeriod")
                        .addParametersItem(queryParam("levelName", "string", "Уровень: TRACE, DEBUG, INFO, WARN, ERROR, FATAL"))
                        .addParametersItem(queryParam("from", "string", "Начало периода, ISO: 2026-11-01T00:00:00"))
                        .addParametersItem(queryParam("to", "string", "Конец периода, ISO: 2026-12-31T23:59:59"))
                        .responses(new ApiResponses().addApiResponse("200", halResponse("Найденные записи")))));
    }

    private Operation operation(String summary, String operationId) {
        return new Operation().tags(List.of(DATA_REST_TAG)).summary(summary).operationId(operationId);
    }

    private Parameter queryParam(String name, String type, String description) {
        return new Parameter().name(name).in("query").required(false)
                .description(description).schema(new Schema<>().type(type));
    }

    private ApiResponse halResponse(String description) {
        return new ApiResponse().description(description)
                .content(new Content().addMediaType(HAL,
                        new MediaType().schema(new Schema<>().type("object"))));
    }

    private Content jsonBody() {
        return new Content().addMediaType("application/json",
                new MediaType().schema(new Schema<>().type("object")));
    }
}