package org.devquality;
import io.javalin.Javalin;


import io.javalin.json.JavalinJackson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.devquality.config.DatabaseConfig;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("🚀 Iniciando aplicación...");

        try {
            // 1. Inicializar configuración de base de datos
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();

            // 2. Ejecutar migraciones con Flyway
            runMigrations(dbConfig);

            // 3. Configurar Jackson para manejar LocalDateTime
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            // 4. Crear aplicación Javalin
            Javalin app = Javalin.create(config -> {
                // JSON
                config.jsonMapper(new JavalinJackson(objectMapper, true));

                // CORS
                config.bundledPlugins.enableCors(cors -> {
                    cors.addRule(CorsPluginConfig.CorsRule::anyHost);
                });

                // Otras configuraciones útiles
                config.showJavalinBanner = false;        // Sin banner al iniciar
                config.useVirtualThreads = true;         // Java 21+ Virtual Threads
                config.requestLogger.http((ctx, ms) -> {
                    logger.info("{} {} - {}ms", ctx.method(), ctx.path(), ms);
                });
            });


            // 7. Iniciar servidor
            int port = Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", "8090"));
            app.start(port);

            logger.info("✅ Aplicación iniciada en http://localhost:{}", port);


            // 8. Graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("🛑 Cerrando aplicación...");
                app.stop();
                dbConfig.close();
                logger.info("✅ Aplicación cerrada correctamente");
            }));

        } catch (Exception e) {
            logger.error("❌ Error fatal al iniciar la aplicación", e);
            System.exit(1);
        }
    }

    private static void runMigrations(DatabaseConfig dbConfig) {
        logger.info("🔄 Ejecutando migraciones de base de datos...");

        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dbConfig.getDataSource())
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .validateOnMigrate(true)
                    .load();

            // Información de migraciones
            var info = flyway.info();
            logger.info("📋 Estado de migraciones:");
            for (var migration : info.all()) {
                logger.info("  {} - {} ({})",
                        migration.getVersion(),
                        migration.getDescription(),
                        migration.getState());
            }

            // Ejecutar migraciones
            var result = flyway.migrate();

            if (result.migrationsExecuted > 0) {
                logger.info("✅ {} migraciones ejecutadas correctamente", result.migrationsExecuted);
            } else {
                logger.info("✅ Base de datos actualizada (no había migraciones pendientes)");
            }

        } catch (Exception e) {
            logger.error("❌ Error al ejecutar migraciones", e);
            throw new RuntimeException("Fallo en migraciones de base de datos", e);
        }
    }

}