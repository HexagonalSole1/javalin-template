package org.devquality.config;

import io.javalin.plugin.bundled.CorsPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class CorsConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(CorsConfiguration.class);

    /**
     * Configura CORS según el entorno
     */
    public static void configureCors(CorsPluginConfig cors) {
        String environment = getEnvironment();
        logger.info("🌐 Configurando CORS para entorno: {}", environment);

        switch (environment.toLowerCase()) {
            case "production":
                configureProductionCors(cors);
                break;
            case "staging":
                configureStagingCors(cors);
                break;
            case "testing":
                configureTestingCors(cors);
                break;
            case "development":
            case "local":
            default:
                configureDevelopmentCors(cors);
                break;
        }

        logger.info("✅ CORS configurado correctamente para {}", environment);
    }

    /**
     * Configuración CORS para desarrollo (muy permisiva)
     */
    private static void configureDevelopmentCors(CorsPluginConfig cors) {
        logger.debug("🔧 Aplicando configuración CORS para desarrollo");

        cors.addRule(CorsPluginConfig.CorsRule::anyHost);

        logger.debug("✅ CORS configurado para desarrollo - Muy permisivo (anyHost)");
    }

    /**
     * Configuración CORS para testing
     */
    private static void configureTestingCors(CorsPluginConfig cors) {
        logger.debug("🔧 Aplicando configuración CORS para testing");

        List<String> allowedOrigins = getTestingOrigins();

        if (allowedOrigins.isEmpty()) {
            cors.addRule(CorsPluginConfig.CorsRule::anyHost); // Fallback para testing
            logger.debug("✅ CORS configurado para testing - Fallback anyHost");
        } else {
            cors.addRule(rule -> {
                allowedOrigins.forEach(rule::allowHost);
            });
            logger.debug("✅ CORS configurado para testing con {} orígenes", allowedOrigins.size());
        }
    }

    /**
     * Configuración CORS para staging (más restrictiva)
     */
    private static void configureStagingCors(CorsPluginConfig cors) {
        logger.debug("🔧 Aplicando configuración CORS para staging");

        List<String> allowedOrigins = getStagingOrigins();

        if (allowedOrigins.isEmpty()) {
            logger.warn("⚠️ No hay orígenes configurados para staging, usando configuración restrictiva");
            cors.addRule(rule -> {
                rule.allowHost("https://staging.tuapp.com");
            });
        } else {
            cors.addRule(rule -> {
                allowedOrigins.forEach(rule::allowHost);
            });
        }

        logger.debug("✅ CORS configurado para staging con {} orígenes", allowedOrigins.size());
    }

    /**
     * Configuración CORS para producción (muy restrictiva)
     */
    private static void configureProductionCors(CorsPluginConfig cors) {
        logger.debug("🔧 Aplicando configuración CORS para producción");

        List<String> allowedOrigins = getProductionOrigins();

        if (allowedOrigins.isEmpty()) {
            logger.error("❌ No hay orígenes configurados para producción - CORS muy restrictivo");
            throw new IllegalStateException("Orígenes CORS no configurados para producción");
        }

        cors.addRule(rule -> {
            allowedOrigins.forEach(rule::allowHost);
        });

        logger.info("✅ CORS configurado para producción con {} orígenes autorizados", allowedOrigins.size());
        logger.debug("🔒 Orígenes autorizados: {}", allowedOrigins);
    }

    /**
     * Obtiene los orígenes permitidos para desarrollo
     */
    private static List<String> getDevelopmentOrigins() {
        return Arrays.asList(
                "http://localhost:3000",  // React default
                "http://localhost:3001",  // React alternate
                "http://localhost:4200",  // Angular default
                "http://localhost:5173",  // Vite default
                "http://localhost:8080",  // Vue default
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173"
        );
    }

    /**
     * Obtiene los orígenes permitidos para testing
     */
    private static List<String> getTestingOrigins() {
        String testOrigins = System.getenv("CORS_TEST_ORIGINS");
        if (testOrigins != null && !testOrigins.trim().isEmpty()) {
            return Arrays.asList(testOrigins.split(","));
        }
        return getDevelopmentOrigins(); // Fallback
    }

    /**
     * Obtiene los orígenes permitidos para staging
     */
    private static List<String> getStagingOrigins() {
        String stagingOrigins = System.getenv("CORS_STAGING_ORIGINS");
        if (stagingOrigins != null && !stagingOrigins.trim().isEmpty()) {
            return Arrays.asList(stagingOrigins.split(","));
        }
        return Arrays.asList(); // Sin orígenes por defecto en staging
    }

    /**
     * Obtiene los orígenes permitidos para producción
     */
    private static List<String> getProductionOrigins() {
        String productionOrigins = System.getenv("CORS_PRODUCTION_ORIGINS");
        if (productionOrigins != null && !productionOrigins.trim().isEmpty()) {
            return Arrays.asList(productionOrigins.split(","));
        }
        return Arrays.asList(); // Sin orígenes por defecto en producción
    }

    /**
     * Obtiene el entorno de ejecución
     */
    private static String getEnvironment() {
        return System.getenv().getOrDefault("ENVIRONMENT",
                System.getenv().getOrDefault("ENV", "development"));
    }

    /**
     * Información sobre la configuración CORS actual
     */
    public static void logCorsInfo() {
        String environment = getEnvironment();
        logger.info("📋 Información CORS:");
        logger.info("  🌍 Entorno: {}", environment);

        switch (environment.toLowerCase()) {
            case "production":
                List<String> prodOrigins = getProductionOrigins();
                logger.info("  🔒 Orígenes permitidos ({}): {}", prodOrigins.size(),
                        prodOrigins.isEmpty() ? "NINGUNO" : prodOrigins);
                break;
            case "staging":
                List<String> stagingOrigins = getStagingOrigins();
                logger.info("  🔧 Orígenes permitidos ({}): {}", stagingOrigins.size(),
                        stagingOrigins.isEmpty() ? "NINGUNO" : stagingOrigins);
                break;
            case "development":
            case "local":
            default:
                logger.info("  🔓 Modo desarrollo: Muy permisivo (anyHost)");
                logger.info("  📝 Orígenes sugeridos para desarrollo: {}", getDevelopmentOrigins());
                break;
        }

        logger.info("💡 Variables de entorno:");
        logger.info("  ENVIRONMENT o ENV: Configura el entorno");
        logger.info("  CORS_PRODUCTION_ORIGINS: Orígenes para producción (separados por comas)");
        logger.info("  CORS_STAGING_ORIGINS: Orígenes para staging (separados por comas)");
        logger.info("  CORS_TEST_ORIGINS: Orígenes para testing (separados por comas)");
    }
}