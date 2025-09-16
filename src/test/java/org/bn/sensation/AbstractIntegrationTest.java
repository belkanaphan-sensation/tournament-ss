package org.bn.sensation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true); // Включаем переиспользование контейнера

    @Autowired
    protected TestDatabaseCleaner databaseCleaner;

    /**
     * Очищает базу данных перед каждым тестом.
     * Переопределите этот метод в тестовых классах, если нужна другая логика очистки.
     */
    protected void cleanDatabase() {
        databaseCleaner.cleanDatabase();
    }
}
