package com.studymate.ai.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Checking and applying database schema migrations for StudyMate...");

        try {
            // Ensure folder table exists
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS folder (
                    folder_id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
                )
            """);

            // Ensure document table has all required columns
            jdbcTemplate.execute("ALTER TABLE document ADD COLUMN IF NOT EXISTS progress INTEGER DEFAULT 0");
            jdbcTemplate.execute("ALTER TABLE document ADD COLUMN IF NOT EXISTS progress_message VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE document ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'UPLOADED'");
            jdbcTemplate.execute("ALTER TABLE document ADD COLUMN IF NOT EXISTS error_message TEXT");
            jdbcTemplate.execute("ALTER TABLE document ADD COLUMN IF NOT EXISTS folder_id BIGINT REFERENCES folder(folder_id) ON DELETE SET NULL");

            log.info("Database schema migration verified successfully.");
        } catch (Exception e) {
            log.warn("Notice during schema migration check: {}", e.getMessage());
        }
    }
}
