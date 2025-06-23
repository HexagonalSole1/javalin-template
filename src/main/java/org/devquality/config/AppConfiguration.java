package org.devquality.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Configuración centralizada de la aplicación
 * Maneja todas las configuraciones desde variables de entorno y properties
 */
public class AppConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(AppConfiguration.class);
    private static AppConfiguration instance;
    private final Map<String, String> config;

    private AppConfiguration() {
        this.config = new HashMap<>();
        loadConfiguration();
    }

    public static synchronized AppConfiguration getInstance() {
        if (instance == null) {
            instance = new AppConfiguration();
        }
        return instance;
    }

    /**
     * Carga toda la configuración de la aplicación
     */
    private void loadConfiguration() {
        logger.info("📋 Cargando configuración de la aplicación...");

        // Configuración del servidor
        loadServerConfiguration();

        // Configuración de base de datos
        loadDatabaseConfiguration();

        // Configuración de CORS
        loadCorsConfiguration();

        // Configuración de Jackson
        loadJacksonConfiguration();

        // Configuración de logging
        loadLoggingConfiguration();

        logger.info("✅ Configuración cargada correctamente");
    }

    /**
     * Configuración del servidor HTTP
     */
    private void loadServerConfiguration() {
        setConfig("server.port", getEnvOrDefault("SERVER_PORT", "8090"));
        setConfig("server.environment", getEnvOrDefault("ENVIRONMENT", getEnvOrDefault("ENV", "development")));
        setConfig("server.virtual-threads", getEnvOrDefault("VIRTUAL_THREADS_ENABLED", "true"));
        setConfig("server.request-logging", getEnvOrDefault("REQUEST_LOGGING_ENABLED", "true"));
    }

    /**
     * Configuración de base de datos
     */
    private void loadDatabaseConfiguration() {
        setConfig("db.host", getEnvOrDefault("DB_HOST", "localhost"));
        setConfig("db.port", getEnvOrDefault("DB_PORT", "5432"));
        setConfig("db.name", getEnvOrDefault("DB_NAME", "users"));
        setConfig("db.user", getEnvOrDefault("DB_USER", "Hexagonal"));
        setConfig("db.password", getEnvOrDefault("DB_PASSWORD", "HexagonalSole89"));
        setConfig("db.pool.max-size", getEnvOrDefault("DB_POOL_MAX_SIZE", "20"));
        setConfig("db.pool.min-idle", getEnvOrDefault("DB_POOL_MIN_IDLE", "5"));
        setConfig("db.connection-timeout", getEnvOrDefault("DB_CONNECTION_TIMEOUT", "30000"));
    }

    /**
     * Configuración de CORS
     */
    private void loadCorsConfiguration() {
        setConfig("cors.production-origins", getEnvOrDefault("CORS_PRODUCTION_ORIGINS", ""));
        setConfig("cors.staging-origins", getEnvOrDefault("CORS_STAGING_ORIGINS", ""));
        setConfig("cors.test-origins", getEnvOrDefault("CORS_TEST_ORIGINS", ""));
        setConfig("cors.allow-credentials", getEnvOrDefault("CORS_ALLOW_CREDENTIALS", "true"));
        setConfig("cors.max-age", getEnvOrDefault("CORS_MAX_AGE", "3600"));
    }

    /**
     * Configuración de Jackson
     */
    private void loadJacksonConfiguration() {
        setConfig("jackson.pretty-print", getEnvOrDefault("JACKSON_PRETTY_PRINT", isDevelopment() ? "true" : "false"));
        setConfig("jackson.naming-strategy", getEnvOrDefault("JACKSON_NAMING_STRATEGY", "camelCase"));
        setConfig("jackson.fail-on-unknown-properties", getEnvOrDefault("JACKSON_FAIL_ON_UNKNOWN_PROPERTIES", "false"));
        setConfig("jackson.date-format", getEnvOrDefault("JACKSON_DATE_FORMAT", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
    }

    /**
     * Configuración de logging
     */
    private void loadLoggingConfiguration() {
        setConfig("logging.level", getEnvOrDefault("LOGGING_LEVEL", "INFO"));
        setConfig("logging.request-details", getEnvOrDefault("LOGGING_REQUEST_DETAILS", isDevelopment() ? "true" : "false"));
        setConfig("logging.sql-queries", getEnvOrDefault("LOGGING_SQL_QUERIES", "false"));
    }

    /**
     * Obtiene un valor de configuración
     */
    public String get(String key) {
        return config.get(key);
    }

    /**
     * Obtiene un valor de configuración con valor por defecto
     */
    public String get(String key, String defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }

    /**
     * Obtiene un valor como entero
     */
    public int getInt(String key) {
        String value = config.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Configuration key not found: " + key);
        }
        return Integer.parseInt(value);
    }

    /**
     * Obtiene un valor como entero con valor por defecto
     */
    public int getInt(String key, int defaultValue) {
        String value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("⚠️ Valor inválido para {}: {}, usando valor por defecto: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Obtiene un valor como boolean
     */
    public boolean getBoolean(String key) {
        String value = config.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Configuration key not found: " + key);
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Obtiene un valor como boolean con valor por defecto
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Verifica si está en modo desarrollo
     */
    public boolean isDevelopment() {
        String environment = get("server.environment", "development");
        return "development".equalsIgnoreCase(environment) || "local".equalsIgnoreCase(environment);
    }

    /**
     * Verifica si está en modo producción
     */
    public boolean isProduction() {
        String environment = get("server.environment", "development");
        return "production".equalsIgnoreCase(environment);
    }

    /**
     * Obtiene el entorno actual
     */
    public String getEnvironment() {
        return get("server.environment", "development");
    }

    /**
     * Establece un valor de configuración
     */
    private void setConfig(String key, String value) {
        config.put(key, value);
    }

    /**
     * Obtiene variable de entorno con valor por defecto
     */
    private String getEnvOrDefault(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Muestra toda la configuración actual (sin passwords)
     */
    public void logConfiguration() {
        logger.info("📋 ================================");
        logger.info("📋 CONFIGURACIÓN DE LA APLICACIÓN");
        logger.info("📋 ================================");

        logger.info("🌍 Entorno: {}", getEnvironment());
        logger.info("🚀 Servidor:");
        logger.info("  Puerto: {}", get("server.port"));
        logger.info("  Virtual Threads: {}", get("server.virtual-threads"));
        logger.info("  Request Logging: {}", get("server.request-logging"));

        logger.info("🗄️ Base de datos:");
        logger.info("  Host: {}", get("db.host"));
        logger.info("  Puerto: {}", get("db.port"));
        logger.info("  Base de datos: {}", get("db.name"));
        logger.info("  Usuario: {}", get("db.user"));
        logger.info("  Pool Max Size: {}", get("db.pool.max-size"));
        logger.info("  Pool Min Idle: {}", get("db.pool.min-idle"));

        logger.info("🌐 CORS:");
        logger.info("  Allow Credentials: {}", get("cors.allow-credentials"));
        logger.info("  Max Age: {}", get("cors.max-age"));

        logger.info("📄 Jackson:");
        logger.info("  Pretty Print: {}", get("jackson.pretty-print"));
        logger.info("  Naming Strategy: {}", get("jackson.naming-strategy"));

        logger.info("📝 Logging:");
        logger.info("  Level: {}", get("logging.level"));
        logger.info("  Request Details: {}", get("logging.request-details"));

        logger.info("📋 ================================");
    }

    /**
     * Valida que toda la configuración requerida esté presente
     */
    public boolean validateConfiguration() {
        logger.info("🔍 Validando configuración...");

        boolean valid = true;

        // Validar configuración crítica
        if (isProduction()) {
            if (get("cors.production-origins", "").isEmpty()) {
                logger.error("❌ CORS_PRODUCTION_ORIGINS no configurado en producción");
                valid = false;
            }
        }

        // Validar puerto
        try {
            getInt("server.port");
        } catch (NumberFormatException e) {
            logger.error("❌ Puerto del servidor inválido: {}", get("server.port"));
            valid = false;
        }

        if (valid) {
            logger.info("✅ Configuración válida");
        } else {
            logger.error("❌ Configuración inválida");
        }

        return valid;
    }

    /**
     * Recarga la configuración (útil para cambios en runtime)
     */
    public synchronized void reload() {
        logger.info("🔄 Recargando configuración...");
        config.clear();
        loadConfiguration();
        logger.info("✅ Configuración recargada");
    }
}