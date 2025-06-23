package org.devquality;

import io.javalin.Javalin;
import org.devquality.config.*;
import org.devquality.routes.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("🚀 Iniciando aplicación...");

        try {
            // 0️⃣ Cargar configuración centralizada
            logger.info("0️⃣ Cargando configuración...");
            AppConfiguration appConfig = AppConfiguration.getInstance();

            // Validar configuración antes de continuar
            if (!appConfig.validateConfiguration()) {
                logger.error("❌ Configuración inválida, abortando inicio");
                System.exit(1);
            }

            // 1️⃣ Inicializar configuración de base de datos
            logger.info("1️⃣ Inicializando base de datos...");
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();

            // 2️⃣ Ejecutar migraciones con Flyway
            logger.info("2️⃣ Ejecutando migraciones...");
            FlywayConfiguration flywayConfig = new FlywayConfiguration(dbConfig);
            flywayConfig.runMigrations();

            // 3️⃣ Crear aplicación Javalin
            logger.info("3️⃣ Configurando servidor web...");
            Javalin app = createJavalinApp(appConfig);

            // 4️⃣ Configurar rutas
            logger.info("4️⃣ Configurando rutas...");
            Routes routes = new Routes(dbConfig);
            routes.configureRoutes(app);

            // 5️⃣ Iniciar servidor
            logger.info("5️⃣ Iniciando servidor...");
            int port = appConfig.getInt("server.port");
            app.start(port);

            // 6️⃣ Mostrar información de la aplicación
            Routes.logApplicationInfo(port);
            appConfig.logConfiguration();

            // 7️⃣ Graceful shutdown
            setupShutdownHook(app, dbConfig);

        } catch (Exception e) {
            logger.error("❌ Error fatal al iniciar la aplicación", e);
            System.exit(1);
        }
    }

    /**
     * Crea y configura la aplicación Javalin usando AppConfiguration
     */
    private static Javalin createJavalinApp(AppConfiguration appConfig) {
        logger.debug("🔧 Creando aplicación Javalin...");

        return Javalin.create(config -> {
            // JSON Configuration
            config.jsonMapper(JacksonConfiguration.createJavalinJackson());

            // CORS Configuration
            config.bundledPlugins.enableCors(CorsConfiguration::configureCors);

            // Server Configuration
            config.showJavalinBanner = false;

            // Virtual Threads (si está habilitado y disponible)
            if (appConfig.getBoolean("server.virtual-threads") && isVirtualThreadsAvailable()) {
                config.useVirtualThreads = true;
                logger.info("🧵 Virtual Threads habilitados");
            } else {
                logger.info("🧵 Virtual Threads deshabilitados");
            }

            // Request Logging (si está habilitado)
            if (appConfig.getBoolean("server.request-logging")) {
                config.requestLogger.http((ctx, ms) -> {
                    if (appConfig.getBoolean("logging.request-details", false)) {
                        logger.info("{} {} - {}ms [{}]",
                                ctx.method(), ctx.path(), ms,
                                ctx.header("User-Agent", "Unknown"));
                    } else {
                        logger.info("{} {} - {}ms", ctx.method(), ctx.path(), ms);
                    }
                });
            }

            // Development Configuration
            if (appConfig.isDevelopment()) {
                config.bundledPlugins.enableDevLogging();
                logger.debug("🔧 Configuración de desarrollo habilitada");
            }
        });
    }

    /**
     * Configura el graceful shutdown
     */
    private static void setupShutdownHook(Javalin app, DatabaseConfig dbConfig) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("🛑 Iniciando cierre controlado de la aplicación...");

            try {
                // Detener servidor HTTP
                logger.info("⏹️ Deteniendo servidor HTTP...");
                app.stop();

                // Cerrar conexiones de base de datos
                logger.info("🔌 Cerrando conexiones de base de datos...");
                dbConfig.close();

                logger.info("✅ Aplicación cerrada correctamente");

            } catch (Exception e) {
                logger.error("❌ Error durante el cierre de la aplicación", e);
            }
        }));
    }

    /**
     * Verifica si los virtual threads están disponibles (Java 21+)
     */
    private static boolean isVirtualThreadsAvailable() {
        try {
            String javaVersion = System.getProperty("java.version");
            String[] parts = javaVersion.split("\\.");
            int majorVersion = Integer.parseInt(parts[0]);
            return majorVersion >= 21;
        } catch (Exception e) {
            logger.warn("⚠️ No se pudo determinar la versión de Java: {}", e.getMessage());
            return false;
        }
    }
}