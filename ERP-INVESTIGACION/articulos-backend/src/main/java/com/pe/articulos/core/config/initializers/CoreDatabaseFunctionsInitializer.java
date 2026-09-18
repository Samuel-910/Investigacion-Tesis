package com.pe.articulos.core.config.initializers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoreDatabaseFunctionsInitializer {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void run() {
        log.info("🚀 Inicializando funciones y triggers de PostgreSQL...");
        try {
            // Función para pg_notify
            String functionSql = """
                CREATE OR REPLACE FUNCTION notify_evento_insert()
                RETURNS trigger AS $$
                BEGIN
                    PERFORM pg_notify('nuevo_evento_outbox', row_to_json(NEW)::text);
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql;
                """;
            
            jdbcTemplate.execute(functionSql);

            // Eliminar trigger anterior si existe
            jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_notify_evento_insert ON cola_eventos;");

            // Crear el trigger
            String triggerSql = """
                CREATE TRIGGER trg_notify_evento_insert
                AFTER INSERT ON cola_eventos
                FOR EACH ROW
                EXECUTE FUNCTION notify_evento_insert();
                """;
                
            jdbcTemplate.execute(triggerSql);

            log.info("✅ Funciones y triggers de base de datos creados correctamente.");
        } catch (Exception e) {
            log.error("❌ Error al crear funciones y triggers en la BD: {}", e.getMessage());
        }
    }
}
