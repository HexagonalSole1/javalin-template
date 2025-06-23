package org.devquality.web.validators;


import org.devquality.web.dtos.core.response.ErrorDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Servicio centralizado para validaciones usando Bean Validation (JSR-303/JSR-380)
 */
public class ValidationService {
    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);
    private static final Validator validator;

    // Inicialización del validador (singleton)
    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
            logger.info("✅ Bean Validation inicializado correctamente");
        } catch (Exception e) {
            logger.error("❌ Error al inicializar Bean Validation", e);
            throw new RuntimeException("No se pudo inicializar Bean Validation", e);
        }
    }

    /**
     * Valida un objeto usando Bean Validation sin grupos específicos
     */
    public static <T> ValidationResult validate(T object) {
        if (object == null) {
            List<ErrorDetail> errors = List.of(
                    ErrorDetail.simple("El objeto a validar no puede ser null")
            );
            return new ValidationResult(false, errors);
        }

        logger.debug("🔍 Validando objeto: {}", object.getClass().getSimpleName());

        Set<ConstraintViolation<T>> violations = validator.validate(object);
        List<ErrorDetail> errors = convertViolationsToErrors(violations);

        boolean isValid = errors.isEmpty();
        logger.debug("✅ Validación completada: {} errores encontrados", errors.size());

        return new ValidationResult(isValid, errors);
    }

    /**
     * Valida un objeto usando grupos específicos de validación
     */
    public static <T> ValidationResult validate(T object, Class<?>... groups) {
        if (object == null) {
            List<ErrorDetail> errors = List.of(
                    ErrorDetail.simple("El objeto a validar no puede ser null")
            );
            return new ValidationResult(false, errors);
        }

        logger.debug("🔍 Validando objeto con grupos: {}", object.getClass().getSimpleName());

        Set<ConstraintViolation<T>> violations = validator.validate(object, groups);
        List<ErrorDetail> errors = convertViolationsToErrors(violations);

        boolean isValid = errors.isEmpty();
        logger.debug("✅ Validación por grupos completada: {} errores", errors.size());

        return new ValidationResult(isValid, errors);
    }

    /**
     * Valida un valor específico de una propiedad
     */
    public static <T> ValidationResult validateProperty(T object, String propertyName, Class<?>... groups) {
        if (object == null) {
            List<ErrorDetail> errors = List.of(
                    ErrorDetail.simple("El objeto a validar no puede ser null")
            );
            return new ValidationResult(false, errors);
        }

        logger.debug("🔍 Validando propiedad '{}' de {}", propertyName, object.getClass().getSimpleName());

        Set<ConstraintViolation<T>> violations = validator.validateProperty(object, propertyName, groups);
        List<ErrorDetail> errors = convertViolationsToErrors(violations);

        boolean isValid = errors.isEmpty();
        logger.debug("✅ Validación de propiedad completada: {} errores", errors.size());

        return new ValidationResult(isValid, errors);
    }

    /**
     * Convierte las violaciones de Bean Validation a nuestros ErrorDetail
     */
    private static <T> List<ErrorDetail> convertViolationsToErrors(Set<ConstraintViolation<T>> violations) {
        List<ErrorDetail> errors = new ArrayList<>();

        for (ConstraintViolation<T> violation : violations) {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            Object invalidValue = violation.getInvalidValue();

            // Obtener el código de error de la anotación
            String code = violation.getConstraintDescriptor()
                    .getAnnotation()
                    .annotationType()
                    .getSimpleName()
                    .toUpperCase();

            errors.add(ErrorDetail.validation(field, code, message, invalidValue));

            logger.debug("📝 Error de validación: {} = '{}' -> {}", field, invalidValue, message);
        }

        return errors;
    }

    /**
     * Clase para encapsular el resultado de validación
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<ErrorDetail> errors;

        public ValidationResult(boolean valid, List<ErrorDetail> errors) {
            this.valid = valid;
            this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        }

        public boolean isValid() {
            return valid;
        }

        public List<ErrorDetail> getErrors() {
            return new ArrayList<>(errors);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public int getErrorCount() {
            return errors.size();
        }

        /**
         * Obtiene solo los errores de un campo específico
         */
        public List<ErrorDetail> getErrorsForField(String fieldName) {
            return errors.stream()
                    .filter(error -> fieldName.equals(error.getField()))
                    .toList();
        }

        /**
         * Verifica si hay errores para un campo específico
         */
        public boolean hasErrorsForField(String fieldName) {
            return errors.stream()
                    .anyMatch(error -> fieldName.equals(error.getField()));
        }
    }
}