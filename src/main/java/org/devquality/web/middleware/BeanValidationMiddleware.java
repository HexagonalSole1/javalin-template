package org.devquality.web.middleware;


import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.devquality.web.dtos.core.response.BaseResponse;
import org.devquality.web.dtos.core.response.ErrorDetail;
import org.devquality.web.validators.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Middleware centralizado que maneja TODA la validación y parsing
 * para mantener los controllers súper limpios
 */
public class BeanValidationMiddleware {
    private static final Logger logger = LoggerFactory.getLogger(BeanValidationMiddleware.class);

    /**
     * 🎯 MÉTODO PRINCIPAL: Valida request completo usando Bean Validation
     *
     * @param ctx Context de Javalin
     * @param clazz Clase del DTO a deserializar
     * @param groups Grupos de validación a aplicar
     * @return Objeto validado o null si hay errores (ya responde automáticamente)
     */
    public static <T> T validateRequest(Context ctx, Class<T> clazz, Class<?>... groups) {
        try {
            logger.debug("🔍 Iniciando validación completa para {}", clazz.getSimpleName());

            // 1️⃣ Validar Content-Type
            if (!validateContentType(ctx)) {
                return null; // Ya respondió
            }

            // 2️⃣ Parsear JSON a objeto
            T request = parseJsonBody(ctx, clazz);
            if (request == null) {
                return null; // Ya respondió con error de parsing
            }

            // 3️⃣ Validar usando Bean Validation
            ValidationService.ValidationResult result = ValidationService.validate(request, groups);

            // 4️⃣ Si hay errores, responder automáticamente
            if (result.hasErrors()) {
                logger.warn("❌ Errores de validación encontrados: {}", result.getErrorCount());

                ctx.status(HttpStatus.BAD_REQUEST).json(
                        BaseResponse.validationError("Errores de validación", result.getErrors())
                );
                return null;
            }

            logger.debug("✅ Validación completa exitosa para {}", clazz.getSimpleName());
            return request;

        } catch (Exception e) {
            logger.error("❌ Error inesperado en validación: {}", e.getMessage(), e);

            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(
                    BaseResponse.error("Error interno al procesar request")
            );
            return null;
        }
    }

    /**
     * 🎯 Valida y parsea un ID desde path parameter
     *
     * @param ctx Context de Javalin
     * @param paramName Nombre del parámetro (ej: "id")
     * @return ID parseado o null si hay errores (ya responde automáticamente)
     */
    public static Long validateId(Context ctx, String paramName) {
        try {
            String idParam = ctx.pathParam(paramName);
            logger.debug("🔍 Validando ID parameter '{}': {}", paramName, idParam);

            // Validar que no sea null o vacío
            if (idParam == null || idParam.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST).json(
                        BaseResponse.error("El parámetro '" + paramName + "' es obligatorio")
                );
                return null;
            }

            // Parsear a Long
            long id = Long.parseLong(idParam.trim());

            // Validar que sea positivo
            if (id <= 0) {
                ctx.status(HttpStatus.BAD_REQUEST).json(
                        BaseResponse.error("El '" + paramName + "' debe ser un número positivo")
                );
                return null;
            }

            logger.debug("✅ ID validado correctamente: {}", id);
            return id;

        } catch (NumberFormatException e) {
            logger.warn("❌ ID inválido '{}': {}", paramName, e.getMessage());

            ctx.status(HttpStatus.BAD_REQUEST).json(
                    BaseResponse.error("El '" + paramName + "' debe ser un número válido")
            );
            return null;
        }
    }

    /**
     * 🎯 Maneja errores de base de datos de forma inteligente
     */
    public static void handleDatabaseError(Context ctx, SQLException e) {
        logger.error("❌ Error de base de datos: {}", e.getMessage());

        // Manejar errores comunes de PostgreSQL
        String message = e.getMessage().toLowerCase();

        if (message.contains("duplicate key") || message.contains("unique constraint")) {
            // Error de clave duplicada
            String field = extractFieldFromUniqueError(message);
            String errorMessage = field != null ?
                    "Ya existe un registro con ese " + field :
                    "Ya existe un registro con esos datos";

            ctx.status(HttpStatus.CONFLICT).json(
                    BaseResponse.error(errorMessage)
            );

        } else if (message.contains("foreign key constraint")) {
            // Error de clave foránea
            ctx.status(HttpStatus.BAD_REQUEST).json(
                    BaseResponse.error("La operación viola una restricción de integridad")
            );

        } else if (message.contains("not null constraint")) {
            // Error de campo obligatorio
            String field = extractFieldFromNotNullError(message);
            String errorMessage = field != null ?
                    "El campo '" + field + "' es obligatorio" :
                    "Faltan campos obligatorios";

            ctx.status(HttpStatus.BAD_REQUEST).json(
                    BaseResponse.error(errorMessage)
            );

        } else {
            // Error genérico de base de datos
            handleError(ctx, "Error de base de datos", e);
        }
    }

    /**
     * 🎯 Manejo genérico de errores con detalles en desarrollo
     */
    public static void handleError(Context ctx, String message, Exception e) {
        List<ErrorDetail> errorDetails = new ArrayList<>();

        // En desarrollo, incluir detalles técnicos
        String env = System.getenv().getOrDefault("ENV", "development");
        if ("development".equals(env)) {
            errorDetails.add(ErrorDetail.simple("Detalle técnico: " + e.getMessage()));

            // En desarrollo también loggear el stack trace
            logger.error("❌ Stack trace completo:", e);
        }

        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(
                BaseResponse.error(message, errorDetails.isEmpty() ? null : errorDetails)
        );
    }

    // 🔧 MÉTODOS PRIVADOS DE UTILIDAD

    /**
     * Valida que el Content-Type sea application/json
     */
    private static boolean validateContentType(Context ctx) {
        String contentType = ctx.header("Content-Type");

        if (contentType == null || !contentType.contains("application/json")) {
            logger.warn("❌ Content-Type inválido: {}", contentType);

            ctx.status(HttpStatus.BAD_REQUEST).json(
                    BaseResponse.error("Content-Type debe ser application/json")
            );
            return false;
        }

        return true;
    }

    /**
     * Parsea el body JSON a un objeto
     */
    private static <T> T parseJsonBody(Context ctx, Class<T> clazz) {
        try {
            String body = ctx.body();

            // Validar que el body no esté vacío
            if (body == null || body.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST).json(
                        BaseResponse.error("El body del request no puede estar vacío")
                );
                return null;
            }

            // Parsear con Jackson
            T request = ctx.bodyAsClass(clazz);
            logger.debug("✅ JSON parseado correctamente a {}", clazz.getSimpleName());

            return request;

        } catch (Exception e) {
            logger.warn("❌ Error al parsear JSON: {}", e.getMessage());

            String errorMessage = "JSON inválido";
            if (e.getMessage() != null && e.getMessage().contains("Unexpected character")) {
                errorMessage += ": formato incorrecto";
            } else if (e.getMessage() != null && e.getMessage().contains("Unexpected end-of-input")) {
                errorMessage += ": JSON incompleto";
            }

            ctx.status(HttpStatus.BAD_REQUEST).json(
                    BaseResponse.error(errorMessage)
            );
            return null;
        }
    }

    /**
     * Extrae el nombre del campo de errores unique constraint de PostgreSQL
     */
    private static String extractFieldFromUniqueError(String errorMessage) {
        try {
            // Buscar patrón: detail: key (campo)=(valor) already exists
            if (errorMessage.contains("key (") && errorMessage.contains(")=")) {
                int start = errorMessage.indexOf("key (") + 5;
                int end = errorMessage.indexOf(")", start);
                return errorMessage.substring(start, end);
            }
        } catch (Exception e) {
            logger.debug("No se pudo extraer campo de error unique: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extrae el nombre del campo de errores not null constraint de PostgreSQL
     */
    private static String extractFieldFromNotNullError(String errorMessage) {
        try {
            // Buscar patrón: null value in column "campo" violates not-null constraint
            if (errorMessage.contains("column \"") && errorMessage.contains("\" violates")) {
                int start = errorMessage.indexOf("column \"") + 8;
                int end = errorMessage.indexOf("\"", start);
                return errorMessage.substring(start, end);
            }
        } catch (Exception e) {
            logger.debug("No se pudo extraer campo de error not null: {}", e.getMessage());
        }
        return null;
    }
}
