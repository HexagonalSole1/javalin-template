package org.devquality.web.controllers;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.devquality.services.IUserService;
import org.devquality.web.dtos.users.request.CreaterUserRequest;
import org.devquality.web.dtos.users.response.CreateUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users - Obtener todos los usuarios
     */
    public void getAllUsers(Context ctx) {
        try {
            logger.info("📋 Obteniendo todos los usuarios");

            // Obtener usuarios desde el service
            var users = userService.getAllUsers();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuarios obtenidos correctamente");
            response.put("data", users);
            response.put("count", users.size());

            ctx.status(HttpStatus.OK).json(response);
            logger.info("✅ {} usuarios obtenidos correctamente", users.size());

        } catch (Exception e) {
            logger.error("❌ Error al obtener usuarios: {}", e.getMessage());
            handleError(ctx, "Error al obtener usuarios", e);
        }
    }

    /**
     * POST /api/users - Crear un nuevo usuario
     */
    public void createUser(Context ctx) {
        try {
            logger.info("🆕 Creando nuevo usuario");

            // Validar content-type
            String contentType = ctx.header("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                        "success", false,
                        "message", "Content-Type debe ser application/json"
                ));
                return;
            }

            // Deserializar request
            CreaterUserRequest request = ctx.bodyAsClass(CreaterUserRequest.class);

            // Validaciones básicas
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                        "success", false,
                        "message", "El nombre es obligatorio"
                ));
                return;
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                        "success", false,
                        "message", "El email es obligatorio"
                ));
                return;
            }

            // Validación de formato de email básica
            if (!request.getEmail().contains("@")) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                        "success", false,
                        "message", "El email debe tener un formato válido"
                ));
                return;
            }

            // Crear usuario a través del service
            CreateUserResponse userResponse = userService.user(request);

            // Respuesta exitosa
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario creado correctamente");
            response.put("data", userResponse);

            ctx.status(HttpStatus.CREATED).json(response);
            logger.info("✅ Usuario creado: {}", userResponse.getEmail());

        } catch (SQLException e) {
            logger.error("❌ Error de base de datos al crear usuario: {}", e.getMessage());

            // Manejar errores específicos de base de datos
            if (e.getMessage().contains("duplicate key") || e.getMessage().contains("unique constraint")) {
                ctx.status(HttpStatus.CONFLICT).json(Map.of(
                        "success", false,
                        "message", "Ya existe un usuario con ese email"
                ));
            } else {
                handleError(ctx, "Error de base de datos al crear usuario", e);
            }

        } catch (Exception e) {
            logger.error("❌ Error inesperado al crear usuario: {}", e.getMessage());
            handleError(ctx, "Error al crear usuario", e);
        }
    }

    /**
     * GET /api/users/{id} - Obtener usuario por ID
     */
    public void getUserById(Context ctx) {
        try {
            String idParam = ctx.pathParam("id");
            logger.info("🔍 Obteniendo usuario con ID: {}", idParam);

            // Validar que el ID sea un número
            Long userId;
            try {
                userId = Long.parseLong(idParam);
            } catch (NumberFormatException e) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                        "success", false,
                        "message", "El ID debe ser un número válido"
                ));
                return;
            }

            // Obtener usuario desde el service
            var user = userService.getUserById(userId);

            if (user == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(Map.of(
                        "success", false,
                        "message", "Usuario no encontrado",
                        "id", userId
                ));
                return;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario encontrado");
            response.put("data", user);

            ctx.status(HttpStatus.OK).json(response);
            logger.info("✅ Usuario {} consultado: {}", userId, user.getEmail());

        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Argumento inválido: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("❌ Error al obtener usuario: {}", e.getMessage());
            handleError(ctx, "Error al obtener usuario", e);
        }
    }

    /**
     * GET /api/health - Health check
     */
    public void healthCheck(Context ctx) {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "UserService");
        health.put("timestamp", System.currentTimeMillis());

        ctx.status(HttpStatus.OK).json(health);
        logger.debug("🏥 Health check realizado");
    }

    /**
     * Manejo centralizado de errores
     */
    private void handleError(Context ctx, String message, Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());

        // En desarrollo, incluir detalles del error
        String env = System.getenv().getOrDefault("ENV", "development");
        if ("development".equals(env)) {
            errorResponse.put("error", e.getMessage());
        }

        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(errorResponse);
    }
}