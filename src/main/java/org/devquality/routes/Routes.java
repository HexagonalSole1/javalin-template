package org.devquality.routes;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import org.devquality.config.DatabaseConfig;
import org.devquality.persistence.repositories.IProductRepository;
import org.devquality.persistence.repositories.IUserRepository;
import org.devquality.persistence.repositories.impl.ProductRepository;
import org.devquality.persistence.repositories.impl.UserRepositoryImpl;
import org.devquality.services.IProductService;
import org.devquality.services.IUserService;
import org.devquality.services.impl.ProductServiceImpl;
import org.devquality.services.impl.UserServiceImpl;
import org.devquality.web.controllers.ProductController;
import org.devquality.web.controllers.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Routes {
    private static final Logger logger = LoggerFactory.getLogger(Routes.class);
    private final DatabaseConfig databaseConfig;

    public Routes(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    /**
     * Configura todas las rutas de la aplicación
     */
    public void configureRoutes(Javalin app) {
        logger.info("🌐 Configurando todas las rutas de la aplicación...");

        // 🔧 Inyección de dependencias manual para Users
        IUserRepository userRepository = new UserRepositoryImpl(databaseConfig);
        IUserService userService = new UserServiceImpl(userRepository);
        UserController userController = new UserController(userService);

        // 🔧 Inyección de dependencias manual para Products
        IProductRepository productRepository = new ProductRepository(databaseConfig);
        IProductService productService = new ProductServiceImpl(productRepository);
        ProductController productController = new ProductController(productService);

        // 📋 Configurar rutas de usuarios
        UserRoutes userRoutes = new UserRoutes(userController);
        userRoutes.configure(app);

        // 📦 Configurar rutas de productos
        ProductRoutes productRoutes = new ProductRoutes(productController);
        productRoutes.configure(app);

        // 🏠 Ruta de bienvenida principal
        configureWelcomeRoute(app);

        // ⚡ Configurar manejo de errores globales
        configureErrorHandling(app);

        // 🔍 Configurar middleware de logging
        configureLoggingMiddleware(app);

        logger.info("✅ Todas las rutas configuradas correctamente");
    }

    /**
     * Configura la ruta de bienvenida principal
     */
    private void configureWelcomeRoute(Javalin app) {
        app.get("/", ctx -> {
            ctx.json(java.util.Map.of(
                    "message", "🚀 API REST - Javalin + PostgreSQL + Arquitectura Hexagonal",
                    "version", "1.0.0",
                    "services", java.util.Map.of(
                            "users", "Gestión de usuarios",
                            "products", "Gestión de productos"
                    ),
                    "endpoints", java.util.Map.of(
                            "users", java.util.List.of(
                                    "GET /api/users - Listar usuarios",
                                    "POST /api/users - Crear usuario",
                                    "GET /api/users/{id} - Obtener usuario",
                                    "GET /api/health - Health check usuarios"
                            ),
                            "products", java.util.List.of(
                                    "GET /api/products - Listar productos",
                                    "POST /api/products - Crear producto",
                                    "GET /api/products/{id} - Obtener producto",
                                    "PUT /api/products/{id} - Actualizar producto",
                                    "DELETE /api/products/{id} - Eliminar producto",
                                    "GET /api/products/search?name=... - Buscar por nombre",
                                    "GET /api/products/price-range?min=...&max=... - Buscar por precio",
                                    "GET /api/products/health - Health check productos"
                            )
                    ),
                    "examples", java.util.Map.of(
                            "create_user", java.util.Map.of(
                                    "method", "POST",
                                    "url", "/api/users",
                                    "body", java.util.Map.of(
                                            "name", "Juan Pérez",
                                            "email", "juan@example.com"
                                    )
                            ),
                            "create_product", java.util.Map.of(
                                    "method", "POST",
                                    "url", "/api/products",
                                    "body", java.util.Map.of(
                                            "name", "Laptop Gaming",
                                            "price", 1299.99,
                                            "description", "Laptop para gaming de alta gama"
                                    )
                            )
                    )
            ));
        });
    }

    /**
     * Configura el manejo global de errores
     */
    private void configureErrorHandling(Javalin app) {
        // ⚡ Middleware global para manejo de errores
        app.exception(Exception.class, (exception, ctx) -> {
            logger.error("❌ Error no manejado: {}", exception.getMessage(), exception);

            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(java.util.Map.of(
                    "success", false,
                    "message", "Error interno del servidor",
                    "timestamp", System.currentTimeMillis()
            ));
        });

        // 🚫 Manejo de rutas no encontradas (404)
        app.error(404, ctx -> {
            logger.warn("🚫 Ruta no encontrada: {} {}", ctx.method(), ctx.path());

            ctx.json(java.util.Map.of(
                    "success", false,
                    "message", "Ruta no encontrada",
                    "path", ctx.path(),
                    "method", ctx.method(),
                    "available_endpoints", java.util.Map.of(
                            "general", java.util.List.of(
                                    "GET /"
                            ),
                            "users", java.util.List.of(
                                    "GET /api/health",
                                    "GET /api/users",
                                    "POST /api/users",
                                    "GET /api/users/{id}"
                            ),
                            "products", java.util.List.of(
                                    "GET /api/products/health",
                                    "GET /api/products",
                                    "POST /api/products",
                                    "GET /api/products/{id}",
                                    "PUT /api/products/{id}",
                                    "DELETE /api/products/{id}",
                                    "GET /api/products/search?name=...",
                                    "GET /api/products/price-range?min=...&max=..."
                            )
                    )
            ));
        });

        // 🚫 Manejo de métodos no permitidos (405)
        app.error(405, ctx -> {
            logger.warn("🚫 Método no permitido: {} {}", ctx.method(), ctx.path());

            ctx.json(java.util.Map.of(
                    "success", false,
                    "message", "Método no permitido para esta ruta",
                    "path", ctx.path(),
                    "method", ctx.method()
            ));
        });
    }

    /**
     * Configura el middleware de logging de requests
     */
    private void configureLoggingMiddleware(Javalin app) {
        // 🔍 Middleware para logging de requests y timing
        app.before(ctx -> {
            ctx.attribute("request-start", System.currentTimeMillis());
            logger.debug("📥 {} {} - {}",
                    ctx.method(),
                    ctx.path(),
                    ctx.header("User-Agent", "Unknown")
            );
        });

        // 📊 Middleware para logging de responses
        app.after(ctx -> {
            Long startTime = ctx.attribute("request-start");
            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

            logger.debug("📤 {} {} -> {} ({}ms)",
                    ctx.method(),
                    ctx.path(),
                    ctx.status(),
                    duration
            );
        });
    }

    /**
     * Información de estado de la aplicación completa
     */
    public static void logApplicationInfo(int port) {
        logger.info("🌟 ================================");
        logger.info("🚀 APLICACIÓN INICIADA CORRECTAMENTE");
        logger.info("🌟 ================================");
        logger.info("🌐 URL: http://localhost:{}", port);

        logger.info("👥 Endpoints de USUARIOS:");
        logger.info("   GET    http://localhost:{}/api/health       - Health check", port);
        logger.info("   GET    http://localhost:{}/api/users        - Listar usuarios", port);
        logger.info("   POST   http://localhost:{}/api/users        - Crear usuario", port);
        logger.info("   GET    http://localhost:{}/api/users/1      - Obtener usuario por ID", port);

        logger.info("📦 Endpoints de PRODUCTOS:");
        logger.info("   GET    http://localhost:{}/api/products/health - Health check productos", port);
        logger.info("   GET    http://localhost:{}/api/products      - Listar productos", port);
        logger.info("   POST   http://localhost:{}/api/products      - Crear producto", port);
        logger.info("   GET    http://localhost:{}/api/products/1    - Obtener producto por ID", port);
        logger.info("   PUT    http://localhost:{}/api/products/1    - Actualizar producto", port);
        logger.info("   DELETE http://localhost:{}/api/products/1    - Eliminar producto", port);
        logger.info("   GET    http://localhost:{}/api/products/search?name=laptop - Buscar por nombre", port);
        logger.info("   GET    http://localhost:{}/api/products/price-range?min=100&max=1000 - Buscar por precio", port);

        logger.info("🌟 ================================");
        logger.info("💡 Ejemplos de uso:");

        logger.info("👤 Crear usuario:");
        logger.info("   curl -X POST http://localhost:{}/api/users \\", port);
        logger.info("   -H \"Content-Type: application/json\" \\");
        logger.info("   -d '{\"name\":\"Juan Pérez\",\"email\":\"juan@example.com\"}'");

        logger.info("📦 Crear producto:");
        logger.info("   curl -X POST http://localhost:{}/api/products \\", port);
        logger.info("   -H \"Content-Type: application/json\" \\");
        logger.info("   -d '{\"name\":\"Laptop\",\"price\":999.99,\"description\":\"Laptop gaming\"}'");

        logger.info("🌟 ================================");

        // Log examples específicos de productos
        ProductRoutes.logProductExamples(port);
    }
}