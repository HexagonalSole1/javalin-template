package org.devquality.routes;

import io.javalin.Javalin;
import org.devquality.web.controllers.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRoutes {
    private static final Logger logger = LoggerFactory.getLogger(UserRoutes.class);
    private final UserController userController;

    public UserRoutes(UserController userController) {
        this.userController = userController;
    }

    /**
     * Configura todas las rutas relacionadas con usuarios
     */
    public void configure(Javalin app) {
        logger.info("🛣️ Configurando rutas de usuarios...");

        app.get("/api/health", userController::healthCheck);
        app.get("/api/users", userController::getAllUsers);           // GET - Obtener todos los usuarios
        app.post("/api/users", userController::createUser);           // POST - Crear usuario
        app.get("/api/users/{id}", userController::getUserById);      // GET - Obtener usuario por ID


        logger.info("✅ Rutas de usuarios configuradas correctamente");
        logAvailableRoutes();
    }

    /**
     * Log de todas las rutas disponibles para debugging
     */
    private void logAvailableRoutes() {
        logger.info("📋 Rutas de usuarios disponibles:");
        logger.info("  GET    /api/health       - Health check");
        logger.info("  GET    /api/users        - Obtener todos los usuarios");
        logger.info("  POST   /api/users        - Crear nuevo usuario");
        logger.info("  GET    /api/users/:id    - Obtener usuario por ID");
    }
}