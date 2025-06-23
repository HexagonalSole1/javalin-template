package org.devquality.config;

import org.devquality.config.DatabaseConfig;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlywayConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(FlywayConfiguration.class);
    private final DatabaseConfig databaseConfig;

    public FlywayConfiguration(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    /**
     * Ejecuta las migraciones de base de datos con Flyway
     */
    public void runMigrations() {
        logger.info("🔄 Ejecutando migraciones de base de datos...");

        try {
            Flyway flyway = createFlywayInstance();

            // 🧹 LIMPIEZA COMPLETA (solo para desarrollo)
            //cleanDatabase(flyway);

            // 🚀 Ejecutar migraciones desde cero
            executeMigrations(flyway);

            // 📊 Mostrar estado final
            showFinalStatus(flyway);

        } catch (Exception e) {
            logger.error("❌ Error al ejecutar migraciones", e);
            throw new RuntimeException("Fallo en migraciones de base de datos", e);
        }
    }

    /**
     * Crea y configura la instancia de Flyway
     */
    private Flyway createFlywayInstance() {
        logger.debug("🔧 Configurando Flyway...");

        return Flyway.configure()
                .dataSource(databaseConfig.getDataSource())
                .locations("classpath:db/migration")
                .cleanDisabled(false) // Permitir clean para desarrollo
                .baselineOnMigrate(false) // ❌ DESACTIVAR baseline
                .validateOnMigrate(true)
                .encoding("UTF-8")
                .connectRetries(3)
                .connectRetriesInterval(10)
                .load();
    }

    /**
     * Limpia la base de datos (solo para desarrollo)
     */
    private void cleanDatabase(Flyway flyway) {
        String environment = getEnvironment();

        if ("development".equals(environment) || "local".equals(environment)) {
            logger.info("🧹 Limpiando base de datos completamente (entorno: {})...", environment);
            flyway.clean();

            // 📋 Verificar estado después de clean
            var infoAfterClean = flyway.info();
            logger.info("📋 Estado después de clean:");
            for (var migration : infoAfterClean.all()) {
                logger.info("  {} - {} ({})",
                        migration.getVersion(),
                        migration.getDescription(),
                        migration.getState());
            }
        } else {
            logger.info("⚠️ Clean deshabilitado en entorno: {}", environment);
        }
    }

    /**
     * Ejecuta las migraciones pendientes
     */
    private void executeMigrations(Flyway flyway) {
        logger.info("🚀 Ejecutando migraciones pendientes...");

        var result = flyway.migrate();

        if (result.migrationsExecuted > 0) {
            logger.info("✅ {} migraciones ejecutadas correctamente", result.migrationsExecuted);

            // Log de migraciones ejecutadas
            if (result.migrations != null && !result.migrations.isEmpty()) {
                logger.info("📋 Migraciones ejecutadas:");
                result.migrations.forEach(migration ->
                        logger.info("  {} - {}",
                                migration.version,
                                migration.description)
                );
            }
        } else {
            logger.info("✅ Base de datos actualizada (no había migraciones pendientes)");
        }
    }

    /**
     * Muestra el estado final de las migraciones
     */
    private void showFinalStatus(Flyway flyway) {
        logger.info("📊 Estado final de migraciones:");

        var finalInfo = flyway.info();
        for (var migration : finalInfo.all()) {
            logger.info("  {} - {} ({})",
                    migration.getVersion(),
                    migration.getDescription(),
                    migration.getState());
        }

        // Estadísticas
        long pendingCount = finalInfo.pending().length;
        long appliedCount = finalInfo.applied().length;

        logger.info("📊 Resumen: {} aplicadas, {} pendientes", appliedCount, pendingCount);

        if (pendingCount > 0) {
            logger.warn("⚠️ Hay {} migraciones pendientes", pendingCount);
        }
    }

    /**
     * Valida el estado de las migraciones sin ejecutarlas
     */
    public boolean validateMigrations() {
        logger.info("🔍 Validando estado de migraciones...");

        try {
            Flyway flyway = createFlywayInstance();
            flyway.validate();

            logger.info("✅ Validación de migraciones exitosa");
            return true;

        } catch (Exception e) {
            logger.error("❌ Error en validación de migraciones: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene información sobre el estado actual de las migraciones
     */
    public void showMigrationInfo() {
        logger.info("📋 Información de migraciones:");

        try {
            Flyway flyway = createFlywayInstance();
            var info = flyway.info();

            logger.info("📊 Total de migraciones: {}", info.all().length);
            logger.info("📊 Migraciones aplicadas: {}", info.applied().length);
            logger.info("📊 Migraciones pendientes: {}", info.pending().length);

            if (info.current() != null) {
                logger.info("📊 Versión actual: {}", info.current().getVersion());
            }

        } catch (Exception e) {
            logger.error("❌ Error al obtener información de migraciones: {}", e.getMessage());
        }
    }

    /**
     * Repara el estado de las migraciones (útil para resolver conflictos)
     */
    public void repairMigrations() {
        logger.info("🔧 Reparando estado de migraciones...");

        try {
            Flyway flyway = createFlywayInstance();
            flyway.repair();

            logger.info("✅ Reparación de migraciones completada");

        } catch (Exception e) {
            logger.error("❌ Error al reparar migraciones: {}", e.getMessage());
            throw new RuntimeException("Fallo en reparación de migraciones", e);
        }
    }

    /**
     * Obtiene el entorno de ejecución
     */
    private String getEnvironment() {
        return System.getenv().getOrDefault("ENVIRONMENT",
                System.getenv().getOrDefault("ENV", "development"));
    }
}