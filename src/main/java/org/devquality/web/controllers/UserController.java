package org.devquality.web.controllers;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.devquality.services.IUserService;
import org.devquality.web.dtos.core.response.BaseResponse;
import org.devquality.web.dtos.core.response.ResponseMetadata;
import org.devquality.web.dtos.users.request.CreaterUserRequest;
import org.devquality.web.dtos.users.response.CreateUserResponse;
import org.devquality.web.middleware.BeanValidationMiddleware;
import org.devquality.web.validators.groups.ValidationGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collections;


public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    /**
     *  POST /api/users - Crear nuevo usuario
     */
    public void createUser(Context ctx) {

        CreaterUserRequest request = BeanValidationMiddleware.validateRequest(
                ctx,
                CreaterUserRequest.class,
                ValidationGroups.Create.class
        );

        if (request == null) return;

        try {
            CreateUserResponse userResponse = userService.createUser(request);

            ctx.status(HttpStatus.CREATED).json(
                    BaseResponse.success(userResponse, "Usuario creado correctamente")
            );

        } catch (SQLException e) {
            BeanValidationMiddleware.handleDatabaseError(ctx, e);
        } catch (Exception e) {
            BeanValidationMiddleware.handleError(ctx, "Error al crear usuario", e);
        }
    }

    /**
     *  GET /api/users/{id} - Obtener usuario por ID (ULTRA LIMPIO)
     */
    public void getUserById(Context ctx) {
        Long userId = BeanValidationMiddleware.validateId(ctx, "id");
        if (userId == null) return;

        try {
            var user = userService.getUserById(userId);

            if (user == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(
                        BaseResponse.error("Usuario no encontrado")
                );
                return;
            }

            ctx.status(HttpStatus.OK).json(
                    BaseResponse.success(user, "Usuario encontrado")
            );

        } catch (Exception e) {
            BeanValidationMiddleware.handleError(ctx, "Error al obtener usuario", e);
        }
    }

    /**
     *  GET /api/users - Obtener todos los usuarios
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
     *  GET /api/health - Health check
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