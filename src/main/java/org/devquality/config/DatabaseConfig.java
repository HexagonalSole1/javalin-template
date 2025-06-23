package org.devquality.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static DatabaseConfig instance;
    private HikariDataSource dataSource;

    private DatabaseConfig() {
        setupDataSource();
        logger.info("✅ Conexión a PostgreSQL establecida correctamente");
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private void setupDataSource() {
        HikariConfig config = new HikariConfig();

        // 🏗️ Configuración de PostgreSQL
        String host = getEnvOrDefault("DB_HOST", "localhost");
        String port = getEnvOrDefault("DB_PORT", "5432");
        String database = getEnvOrDefault("DB_NAME", "users");
        String username = getEnvOrDefault("DB_USER", "Hexagonal");
        String password = getEnvOrDefault("DB_PASSWORD", "HexagonalSole89");

        // URL de conexión
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        // 🚀 Configuración del Pool HikariCP (Optimizada para PostgreSQL)
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);

        // 🔧 Configuraciones específicas de PostgreSQL
        Properties props = new Properties();
        props.setProperty("currentSchema", "public");
        props.setProperty("ApplicationName", "JavalinAPI");
        props.setProperty("stringtype", "unspecified");
        props.setProperty("prepareThreshold", "0");
        props.setProperty("defaultRowFetchSize", "1000");
        props.setProperty("loginTimeout", "10");
        props.setProperty("connectTimeout", "10");
        props.setProperty("socketTimeout", "0");
        props.setProperty("tcpKeepAlive", "true");
        config.setDataSourceProperties(props);

        // 📊 Configuración de pool naming y validación
        config.setPoolName("JavalinAPI-HikariCP");
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        config.setRegisterMbeans(true);

        try {
            this.dataSource = new HikariDataSource(config);
            logger.info("🏊‍♂️ Pool de conexiones HikariCP inicializado: {}", jdbcUrl);

            // Test inicial de conexión
            try (Connection conn = dataSource.getConnection()) {
                logger.info("✅ Conexión de prueba exitosa a PostgreSQL");
            }

        } catch (Exception e) {
            logger.error("❌ Error al configurar el pool de conexiones", e);
            throw new RuntimeException("No se pudo conectar a PostgreSQL", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource no está disponible");
        }
        return dataSource.getConnection();
    }

    // ✅ Getter para Flyway
    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public String getPoolStats() {
        if (dataSource != null) {
            return String.format(
                    "Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                    dataSource.getHikariPoolMXBean().getActiveConnections(),
                    dataSource.getHikariPoolMXBean().getIdleConnections(),
                    dataSource.getHikariPoolMXBean().getTotalConnections(),
                    dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
        }
        return "Pool no disponible";
    }

    public boolean isHealthy() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1");
            return true;
        } catch (SQLException e) {
            logger.error("❌ Health check falló", e);
            return false;
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("🔒 Cerrando pool de conexiones...");
            dataSource.close();
            logger.info("✅ Pool de conexiones cerrado");
        }
    }

    private String getEnvOrDefault(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        if (value == null || value.trim().isEmpty()) {
            logger.debug("🔧 Usando valor por defecto para {}: {}", envVar, defaultValue);
            return defaultValue;
        }
        logger.debug("🔧 Usando variable de entorno {}: {}", envVar,
                envVar.contains("PASSWORD") ? "***" : value);
        return value;
    }
}