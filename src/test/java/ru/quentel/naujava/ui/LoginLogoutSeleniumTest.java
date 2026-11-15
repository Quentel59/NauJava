package ru.quentel.naujava.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

/**
 * UI-тест сценария входа и выхода через Selenium WebDriver.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoginLogoutSeleniumTest {

    private static WebDriver driver;

    @LocalServerPort
    private int port;

    private String baseUrl;

    @BeforeAll
    static void setupDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // безоконный режим для CI
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterAll
    static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setupBaseUrl() {
        baseUrl = "http://localhost:" + port;
    }

    /** Тест успешного входа в систему. */
    @Test
    void testSuccessfulLogin() {
        // Переходим на страницу входа
        driver.get(baseUrl + "/login");

        // Проверяем, что отображается форма входа
        WebElement loginForm = driver.findElement(By.tagName("form"));
        Assertions.assertNotNull(loginForm);

        // Вводим учётные данные
        WebElement usernameField = driver.findElement(By.name("username"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys("admin");
        passwordField.sendKeys("admin123");
        submitButton.click();

        // Ожидаем редирект на страницу списка записей логов
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("/log-entries/view/list"));

        // Проверяем, что мы на нужной странице
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("/log-entries/view/list"),
                "После входа должен быть редирект на страницу списка записей");

        // Проверяем, что отображается имя пользователя
        WebElement usernameElement = driver.findElement(By.cssSelector("div.links b"));
        Assertions.assertEquals("admin", usernameElement.getText(),
                "Должно отображаться имя авторизованного пользователя");
    }

    /** Тест выхода из системы. */
    @Test
    void testLogout() {
        // Сначала входим
        driver.get(baseUrl + "/login");
        driver.findElement(By.name("username")).sendKeys("admin");
        driver.findElement(By.name("password")).sendKeys("admin123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Ожидаем редирект на главную страницу
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("/log-entries/view/list"));

        // Находим кнопку "Выйти" и нажимаем
        WebElement logoutButton = driver.findElement(By.cssSelector("form[action='/logout'] button[type='submit']"));
        logoutButton.click();

        // Ожидаем редирект на страницу входа с параметром logout
        wait.until(ExpectedConditions.urlContains("/login?logout"));

        // Проверяем, что отображается сообщение о выходе
        String pageSource = driver.getPageSource();
        Assertions.assertTrue(pageSource.contains("Вы вышли из системы"),
                "Должно отображаться сообщение о выходе");

        // Проверяем, что мы на странице входа
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("/login"),
                "После выхода должен быть редирект на страницу входа");
    }

    /** Тест некорректных учётных данных. */
    @Test
    void testInvalidCredentials() {
        // Переходим на страницу входа
        driver.get(baseUrl + "/login");

        // Вводим неверные учётные данные
        driver.findElement(By.name("username")).sendKeys("wronguser");
        driver.findElement(By.name("password")).sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Ожидаем, что останемся на странице входа с ошибкой
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("/login?error"));

        // Проверяем, что отображается сообщение об ошибке
        String pageSource = driver.getPageSource();
        Assertions.assertTrue(pageSource.contains("Неверный логин или пароль"),
                "Должно отображаться сообщение об ошибке входа");
    }
}