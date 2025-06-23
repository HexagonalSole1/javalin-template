package org.devquality.web.controllers;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.devquality.services.IUserService;
import org.devquality.web.dtos.core.response.BaseResponse;
import org.devquality.web.dtos.core.response.ResponseMetadata;
import org.devquality.web.dtos.users.request.CreaterUserRequest;
import org.devquality.web.middleware.BeanValidationMiddleware;
import org.devquality.web.validators.groups.ValidationGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collections;

/**
 * 🎯 Controller ULTRA LIMPIO - Solo lógica de negocio
 * Toda la validación está en BeanValidationMiddleware
 */

public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    /**
     * ✅ POST /api/users - Crear nuevo usuario (ULTRA LIMPIO)
     */
    public void createUser(Context ctx) {
        // ✨ 1 LÍNEA: Validación automática completa
        CreaterUserRequest request = BeanValidationMiddleware.validateRequest(
                ctx,
                CreaterUserRequest.class,
                ValidationGroups.Create.class
        );

        // ✨ 1 LÍNEA: Si hay errores, ya respondió automáticamente
        if (request == null) return;

        try {
            // ✨ SOLO LÓGICA DE NEGOCIO
            var userResponse = userService.user(request);

            // ✨ RESPUESTA DIRECTA
            ctx.status(HttpStatus.CREATED).json(
                    BaseResponse.success(userResponse, "Usuario creado correctamente")
            );

        } catch (SQLException e) {
            // ✨ 1 LÍNEA: Manejo automático de errores de BD
            BeanValidationMiddleware.handleDatabaseError(ctx, e);
        } catch (Exception e) {
            // ✨ 1 LÍNEA: Manejo automático de cualquier error
            BeanValidationMiddleware.handleError(ctx, "Error al crear usuario", e);
        }
    }

    /**
     * ✅ GET /api/users/{id} - Obtener usuario por ID (ULTRA LIMPIO)
     */
    public void getUserById(Context ctx) {
        // ✨ 1 LÍNEA: Validación automática de ID
        Long userId = BeanValidationMiddleware.validateId(ctx, "id");
        if (userId == null) return;

        try {
            // ✨ SOLO LÓGICA DE NEGOCIO
            var user = userService.getUserById(userId);

            if (user == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(
                        BaseResponse.error("Usuario no encontrado")
                );
                return;
            }

            // ✨ RESPUESTA DIRECTA
            ctx.status(HttpStatus.OK).json(
                    BaseResponse.success(user, "Usuario encontrado")
            );

        } catch (Exception e) {
            // ✨ 1 LÍNEA: Manejo automático de errores
            BeanValidationMiddleware.handleError(ctx, "Error al obtener usuario", e);
        }
    }

    /**
     * ✅ GET /api/users - Obtener todos los usuarios (ULTRA LIMPIO)
     */
    public void getAllUsers(Context ctx) {
        try {
            // ✨ SOLO LÓGICA DE NEGOCIO
            var users = userService.getAllUsers();

            // ✨ METADATA OPCIONAL
            ResponseMetadata metadata = ResponseMetadata.builder()
                    .type("USER_LIST")
                    .totalElements((long) users.size())
                    .build();

            // ✨ RESPUESTA DIRECTA
            ctx.status(HttpStatus.OK).json(
                    BaseResponse.success(users, "Usuarios obtenidos correctamente", metadata)
            );

        } catch (Exception e) {
            // ✨ 1 LÍNEA: Manejo automático de errores
            BeanValidationMiddleware.handleError(ctx, "Error al obtener usuarios", e);
        }
    }

    /**
     * ✅ GET /api/health - Health check (ULTRA LIMPIO)
     */
    public void healthCheck(Context ctx) {
        var healthData = java.util.Map.of(
                "status", "UP",
                "service", "UserService",
                "version", "1.0.0"
        );

        ctx.status(HttpStatus.OK).json(
                BaseResponse.success(healthData, "Servicio funcionando correctamente")
        );
    }
}