package com.example.learningplatform.support;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Базовый класс интеграционных тестов: поднимает весь контекст Spring на H2 (профиль test).
 * Тесты специально НЕ @Transactional - иначе не воспроизвести LazyInitializationException,
 * поэтому после каждого теста таблицы чистятся вручную.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestFixtures.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected TestFixtures fixtures;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @AfterEach
    void cleanDatabase() {
        List<String> tables = jdbcTemplate.queryForList(
                "select table_name from information_schema.tables "
                        + "where lower(table_schema) = 'public' and table_type = 'BASE TABLE'",
                String.class);
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        tables.forEach(table -> jdbcTemplate.execute("TRUNCATE TABLE " + table));
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }
}
