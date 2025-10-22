package org.bn.sensation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Утилитный класс для очистки базы данных между тестами.
 * Использует TRUNCATE CASCADE для быстрой очистки всех таблиц.
 */
@Component
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.liquibase.enabled=true"
})
public class TestDatabaseCleaner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanDatabase() {
        // Получаем список всех таблиц в схеме public
        List<String> tables = jdbcTemplate.queryForList(
            "SELECT tablename FROM pg_tables WHERE schemaname = 'public'",
            String.class
        );

        // Очищаем все таблицы с CASCADE для удаления зависимостей
        for (String table : tables) {
            if (!table.equals("databasechangelog") && !table.equals("databasechangeloglock")) {
                jdbcTemplate.execute("TRUNCATE TABLE " + table + " CASCADE");
            }
        }

        // Сбрасываем последовательности
        jdbcTemplate.execute("SELECT setval(pg_get_serial_sequence('users', 'id'), 1, false);");
        jdbcTemplate.execute("SELECT setval(pg_get_serial_sequence('activity', 'id'), 1, false);");
        jdbcTemplate.execute("SELECT setval(pg_get_serial_sequence('activity_user', 'id'), 1, false);");
    }
}
