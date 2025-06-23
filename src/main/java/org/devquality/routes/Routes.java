package org.devquality.routes;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import org.devquality.config.DatabaseConfig;
import org.devquality.persistence.repositories.IUserRepository;
import org.devquality.persistence.repositories.impl.UserRepositoryImpl;
import org.devquality.services.IUserService;
import org.devquality.services.impl.UserServiceImpl;
import org.devquality.web.controllers.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Routes {
    private static final Logger logger = LoggerFactory.getLogger(Routes.class);
    private final DatabaseConfig databaseConfig;

    public Routes(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    /**
     * Configura todas las rutas de la aplicación
     */
    public void configureRoutes(Javalin app) {
        logger.info("🌐 Configurando todas las rutas de la aplicación...");

        // 🔧 Inyección de dependencias manual
        IUserRepository userRepository = new UserRepositoryImpl(databaseConfig);
        IUserService userService = new UserServiceImpl(userRepository);
        UserController userController = new UserController(userService);

        // 📋 Configurar rutas de usuarios
        UserRoutes userRoutes = new UserRoutes(userController);
        userRoutes.configure(app);

        // ⚡ Middleware global para manejo de errores
        app.exception(Exception.class, (exception, ctx) -> {
            logger.error("❌ Error no manejado: {}", exception.getMessage(), exception);

            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(java.util.Map.of(
                    "success", false,
                    "message", "Error interno del servidor",
                    "timestamp", System.currentTimeMillis()
            ));
        });

        // 🚫 Manejo de rutas no encontradas (404)
        app.error(404, ctx -> {
            logger.warn("🚫 Ruta no encontrada: {} {}", ctx.method(), ctx.path());

            ctx.json(java.util.Map.of(
                    "success", false,
                    "message", "Ruta no encontrada",
                    "path", ctx.path(),
                    "method", ctx.method(),
                    "available_endpoints", java.util.List.of(
                            "GET /",
                            "GET /api/health",
                            "GET /api/users",
                            "POST /api/users",
                            "GET /api/users/{id}"
                    )
            ));
        });

        // 🚫 Manejo de métodos no permitidos (405)
        app.error(405, ctx -> {
            logger.warn("🚫 Método no permitido: {} {}", ctx.method(), ctx.path());

            ctx.json(java.util.Map.of(
                    "success", false,
                    "message", "Método no permitido para esta ruta",
                    "path", ctx.path(),
                    "method", ctx.method()
            ));
        });

        // 🔍 Middleware para logging de requests y timing
        app.before(ctx -> {
            ctx.attribute("request-start", System.currentTimeMillis());
            logger.debug("📥 {} {} - {}",
                    ctx.method(),
                    ctx.path(),
                    ctx.header("User-Agent", "Unknown")
            );
        });

        // 📊 Middleware para logging de responses
        app.after(ctx -> {
            Long startTime = ctx.attribute("request-start");
            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

            logger.debug("📤 {} {} -> {} ({}ms)",
                    ctx.method(),
                    ctx.path(),
                    ctx.status(),
                    duration
            );
        });

        logger.info("✅ Todas las rutas configuradas correctamente");
    }

    /**
     * Información de estado de la aplicación
     */
    public static void logApplicationInfo(int port) {
        logger.info("🌟 ================================");
        logger.info("🚀 APLICACIÓN INICIADA CORRECTAMENTE");
        logger.info("🌟 ================================");
        logger.info("🌐 URL: http://localhost:{}", port);
        logger.info("📋 Endpoints disponibles:");
        logger.info("   GET    http://localhost:{}/                 - Página de bienvenida", port);
        logger.info("   GET    http://localhost:{}/api/health       - Health check", port);
        logger.info("   GET    http://localhost:{}/api/users        - Listar usuarios", port);
        logger.info("   POST   http://localhost:{}/api/users        - Crear usuario", port);
        logger.info("   GET    http://localhost:{}/api/users/1      - Obtener usuario por ID", port);
        logger.info("🌟 ================================");
        logger.info("💡 Ejemplo de uso:");
        logger.info("   curl -X POST http://localhost:{}/api/users \\", port);
        logger.info("   -H \"Content-Type: application/json\" \\");
        logger.info("   -d '{\"name\":\"Juan Pérez\",\"email\":\"juan@example.com\"}'");
        logger.info("🌟 ================================");
    }
}