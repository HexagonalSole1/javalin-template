package org.devquality.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class JacksonConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(JacksonConfiguration.class);

    /**
     * Crea y configura un ObjectMapper optimizado para la aplicación
     */
    public static ObjectMapper createObjectMapper() {
        logger.debug("🔧 Configurando ObjectMapper de Jackson...");

        ObjectMapper objectMapper = new ObjectMapper();

        // 📅 Soporte para Java 8 Time API
        configureTimeSupport(objectMapper);

        // 🔧 Configuraciones de serialización
        configureSerializationFeatures(objectMapper);

        // 🔧 Configuraciones de deserialización
        configureDeserializationFeatures(objectMapper);

        // 🏷️ Naming strategy
        configureNamingStrategy(objectMapper);

        logger.info("✅ ObjectMapper configurado correctamente");
        return objectMapper;
    }

    /**
     * Crea JavalinJackson configurado para Javalin
     */
    public static JavalinJackson createJavalinJackson() {
        logger.debug("🔧 Configurando JavalinJackson...");

        ObjectMapper objectMapper = createObjectMapper();

        // Crear JavalinJackson con pretty printing habilitado según el entorno
        boolean prettyPrint = isPrettyPrintEnabled();

        logger.info("✅ JavalinJackson configurado (pretty print: {})", prettyPrint);
        return new JavalinJackson(objectMapper, prettyPrint);
    }

    /**
     * Configura el soporte para fechas y tiempos
     */
    private static void configureTimeSupport(ObjectMapper objectMapper) {
        logger.debug("📅 Configurando soporte para Java Time API...");

        // Registrar módulo de Java Time
        objectMapper.registerModule(new JavaTimeModule());

        // Formato de fecha por defecto
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.setDateFormat(dateFormat);

        // Deshabilitar escritura de fechas como timestamps
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        logger.debug("✅ Soporte para fechas configurado (formato ISO-8601, UTC)");
    }

    /**
     * Configura las características de serialización
     */
    private static void configureSerializationFeatures(ObjectMapper objectMapper) {
        logger.debug("📤 Configurando características de serialización...");

        // Configuraciones de desarrollo/debugging
        if (isDevelopmentMode()) {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            logger.debug("✅ Pretty printing habilitado para desarrollo");
        }

        // Configuraciones generales
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

        // Para BigDecimal y números
        objectMapper.enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);

        logger.debug("✅ Características de serialización configuradas");
    }

    /**
     * Configura las características de deserialización
     */
    private static void configureDeserializationFeatures(ObjectMapper objectMapper) {
        logger.debug("📥 Configurando características de deserialización...");

        // Ser tolerante con propiedades desconocidas
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Ser tolerante con propiedades faltantes
        objectMapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

        // Aceptar números como strings
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        // Para enums
        objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

        logger.debug("✅ Características de deserialización configuradas");
    }

    /**
     * Configura la estrategia de naming (camelCase, snake_case, etc.)
     */
    private static void configureNamingStrategy(ObjectMapper objectMapper) {
        String namingStrategy = getNamingStrategy();

        logger.debug("🏷️ Configurando naming strategy: {}", namingStrategy);

        switch (namingStrategy.toLowerCase()) {
            case "snake_case":
                objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
                break;
            case "kebab-case":
                objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
                break;
            case "lower_camel_case":
            case "camelcase":
            default:
                objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
                break;
        }

        logger.debug("✅ Naming strategy configurado: {}", namingStrategy);
    }

    /**
     * Determina si está habilitado el pretty printing
     */
    private static boolean isPrettyPrintEnabled() {
        String prettyPrint = System.getenv("JACKSON_PRETTY_PRINT");
        if (prettyPrint != null) {
            return Boolean.parseBoolean(prettyPrint);
        }

        // Por defecto, habilitado en desarrollo
        return isDevelopmentMode();
    }

    /**
     * Obtiene la estrategia de naming desde variables de entorno
     */
    private static String getNamingStrategy() {
        return System.getenv().getOrDefault("JACKSON_NAMING_STRATEGY", "camelCase");
    }

    /**
     * Verifica si está en modo desarrollo
     */
    private static boolean isDevelopmentMode() {
        String environment = System.getenv().getOrDefault("ENVIRONMENT",
                System.getenv().getOrDefault("ENV", "development"));
        return "development".equalsIgnoreCase(environment) || "local".equalsIgnoreCase(environment);
    }

    /**
     * Información sobre la configuración de Jackson
     */
    public static void logJacksonInfo() {
        logger.info("📋 Información de Jackson:");
        logger.info("  🎨 Pretty Print: {}", isPrettyPrintEnabled());
        logger.info("  🏷️ Naming Strategy: {}", getNamingStrategy());
        logger.info("  🌍 Entorno: {}", getEnvironment());
        logger.info("  📅 Formato de fecha: ISO-8601 (UTC)");
        logger.info("  🔧 Java Time Module: Habilitado");

        logger.info("💡 Variables de entorno disponibles:");
        logger.info("  JACKSON_PRETTY_PRINT: true/false (default: true en desarrollo)");
        logger.info("  JACKSON_NAMING_STRATEGY: camelCase/snake_case/kebab-case (default: camelCase)");
    }

    /**
     * Obtiene el entorno actual
     */
    private static String getEnvironment() {
        return System.getenv().getOrDefault("ENVIRONMENT",
                System.getenv().getOrDefault("ENV", "development"));
    }
}