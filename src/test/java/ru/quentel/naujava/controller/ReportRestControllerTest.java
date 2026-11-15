package ru.quentel.naujava.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Интеграционные тесты ReportRestController через RestAssured.
 * Приложение поднимается на случайном порту, тесты авторизуются как admin (HTTP Basic).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReportRestControllerTest {

    @LocalServerPort
    private int port;

    @BeforeAll
    static void setupRestAssured() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void setupPort() {
        RestAssured.port = port;
    }

    /**
     * Создаёт отчёт и возвращает его id.
     * ВАЖНО: extract().path() для числа возвращает Integer, поэтому тип int, а не Long.
     */
    private int createReport() {
        return given()
                .auth().basic("admin", "admin123")
                .contentType(ContentType.JSON)
                .when()
                .post("/custom/reports")
                .then()
                .statusCode(202)
                .extract().path("reportId");
    }

    /** Позитивный кейс: создание отчёта возвращает 202 Accepted. */
    @Test
    void testCreateReport() {
        given()
                .auth().basic("admin", "admin123")
                .contentType(ContentType.JSON)
                .when()
                .post("/custom/reports")
                .then()
                .statusCode(202)
                .body("reportId", notNullValue())
                .body("status", equalTo("CREATED"))
                .body("message", containsString("Отчёт создан"));
    }

    /** Позитивный кейс: получение списка отчётов. */
    @Test
    void testGetAllReports() {
        int reportId = createReport();

        given()
                .auth().basic("admin", "admin123")
                .when()
                .get("/custom/reports")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("id", hasItem(reportId));
    }

    /** Негативный кейс: получение несуществующего отчёта — 404. */
    @Test
    void testGetReportNotFound() {
        given()
                .auth().basic("admin", "admin123")
                .when()
                .get("/custom/reports/999999")
                .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("message", containsString("не найден"));
    }

    /** Позитивный кейс: получение содержимого отчёта (HTML). */
    @Test
    void testGetReportContent() throws InterruptedException {
        int reportId = createReport();

        // ждём завершения асинхронного формирования
        Thread.sleep(2000);

        Response response = given()
                .auth().basic("admin", "admin123")
                .when()
                .get("/custom/reports/" + reportId + "/content");

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.getContentType().contains("text/html"));
        Assertions.assertTrue(response.getBody().asString().contains("<table>"));
    }

    /** Негативный кейс: неавторизованный доступ — 401 или редирект на /login. */
    @Test
    void testUnauthorizedAccess() {
        given()
                .redirects().follow(false)   // иначе RestAssured перейдёт на /login и получит 200
                .when()
                .get("/custom/reports")
                .then()
                .statusCode(anyOf(equalTo(401), equalTo(302)));
    }

    /** Позитивный кейс: получение метаданных отчёта (JSON). */
    @Test
    void testGetReportMetadata() {
        int reportId = createReport();

        given()
                .auth().basic("admin", "admin123")
                .when()
                .get("/custom/reports/" + reportId)
                .then()
                .statusCode(200)
                .body("id", equalTo(reportId))
                .body("status", notNullValue())
                .body("createdAt", notNullValue());
    }
}