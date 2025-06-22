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
        createTables();
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
        String database = getEnvOrDefault("DB_NAME", "javalin_api");
        String username = getEnvOrDefault("DB_USER", "postgres");
        String password = getEnvOrDefault("DB_PASSWORD", "password");

        // URL de conexión
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        // 🚀 Configuración del Pool HikariCP (Optimizada para PostgreSQL)
        config.setMaximumPoolSize(20);              // Máximo 20 conexiones
        config.setMinimumIdle(5);                   // Mínimo 5 conexiones activas
        config.setConnectionTimeout(30000);         // 30 segundos para obtener conexión
        config.setIdleTimeout(600000);              // 10 minutos idle antes de cerrar
        config.setMaxLifetime(1800000);             // 30 minutos vida máxima de conexión
        config.setLeakDetectionThreshold(60000);    // Detectar leaks después de 1 minuto

        // 🔧 Configuraciones específicas de PostgreSQL
        Properties props = new Properties();
        props.setProperty("currentSchema", "public");
        props.setProperty("ApplicationName", "JavalinAPI");
        props.setProperty("stringtype", "unspecified");
        props.setProperty("prepareThreshold", "0");  // Usar prepared statements inmediatamente
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

        // 🏷️ JMX para monitoreo (opcional)
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

    private void createTables() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id BIGSERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL UNIQUE,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createUsersEmailIndex = """
            CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)
        """;

        String createProductsTable = """
            CREATE TABLE IF NOT EXISTS products (
                id BIGSERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
                description TEXT,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createProductsNameIndex = """
            CREATE INDEX IF NOT EXISTS idx_products_name ON products(name)
        """;

        // Trigger para updated_at automático
        String createUpdatedAtFunction = """
            CREATE OR REPLACE FUNCTION update_updated_at_column()
            RETURNS TRIGGER AS $$
            BEGIN
                NEW.updated_at = CURRENT_TIMESTAMP;
                RETURN NEW;
            END;
            $$ language 'plpgsql'
        """;

        String createUsersUpdatedAtTrigger = """
            DROP TRIGGER IF EXISTS update_users_updated_at ON users;
            CREATE TRIGGER update_users_updated_at
                BEFORE UPDATE ON users
                FOR EACH ROW
                EXECUTE FUNCTION update_updated_at_column()
        """;

        String createProductsUpdatedAtTrigger = """
            DROP TRIGGER IF EXISTS update_products_updated_at ON products;
            CREATE TRIGGER update_products_updated_at
                BEFORE UPDATE ON products
                FOR EACH ROW
                EXECUTE FUNCTION update_updated_at_column()
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            logger.info("📋 Creando tablas en PostgreSQL...");

            // Crear tablas
            stmt.execute(createUsersTable);
            stmt.execute(createUsersEmailIndex);
            stmt.execute(createProductsTable);
            stmt.execute(createProductsNameIndex);

            // Crear función y triggers para updated_at
            stmt.execute(createUpdatedAtFunction);
            stmt.execute(createUsersUpdatedAtTrigger);
            stmt.execute(createProductsUpdatedAtTrigger);

            logger.info("✅ Tablas creadas correctamente");

        } catch (SQLException e) {
            logger.error("❌ Error al crear tablas", e);
            throw new RuntimeException("Error al crear tablas en PostgreSQL", e);
        }
    }

    /**
     * Obtiene información del estado del pool de conexiones
     */
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

    /**
     * Verifica si la conexión a la base de datos está disponible
     */
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

    /**
     * Cierra el pool de conexiones
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("🔒 Cerrando pool de conexiones...");
            dataSource.close();
            logger.info("✅ Pool de conexiones cerrado");
        }
    }

    /**
     * Obtiene variable de entorno o valor por defecto
     */
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