package com.gateway.config;

import com.gateway.config.AppLogger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Order(1)
public class SchemaSanityCheckRunner implements CommandLineRunner {

    private static final AppLogger log = AppLogger.get(SchemaSanityCheckRunner.class);
    private static final Set<String> REQUIRED_TABLES = Set.of("orders", "audit_logs");

    private final JdbcTemplate jdbcTemplate;

    public SchemaSanityCheckRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        log.status("SCHEMA-CHECK", "STARTING", "Validating required auto-generated tables: " + REQUIRED_TABLES);

        Set<String> existingTables = new HashSet<>();

        jdbcTemplate.execute((Connection connection) -> {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();

            try (ResultSet rs = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    if (tableName != null) {
                        existingTables.add(tableName.toLowerCase());
                    }
                }
            }
            return null;
        });

        List<String> missingTables = new ArrayList<>();
        for (String table : REQUIRED_TABLES) {
            if (!existingTables.contains(table)) {
                missingTables.add(table);
            }
        }

        if (!missingTables.isEmpty()) {
            String errorMsg = String.format("Missing tables: %s. Found tables: %s", missingTables, existingTables);
            log.status("SCHEMA-CHECK", "FAILED", errorMsg);
            throw new IllegalStateException("Startup schema sanity check failed: " + errorMsg);
        }

        log.status("SCHEMA-CHECK", "PASSED", "All target tables successfully verified: " + REQUIRED_TABLES);
    }
}