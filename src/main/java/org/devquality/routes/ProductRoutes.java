package org.devquality.routes;

import io.javalin.Javalin;
import org.devquality.web.controllers.ProductController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductRoutes {
    private static final Logger logger = LoggerFactory.getLogger(ProductRoutes.class);
    private final ProductController productController;

    public ProductRoutes(ProductController productController) {
        this.productController = productController;
    }

    /**
     * Configura todas las rutas relacionadas con productos
     */
    public void configure(Javalin app) {
        logger.info("🛣️ Configurando rutas de productos...");

        // 🏥 Health check específico para productos
        app.get("/api/products/health", productController::healthCheck);

        // 🔍 Rutas de búsqueda (DEBEN IR ANTES que las rutas con {id})
        app.get("/api/products/search", productController::searchProductsByName);
        app.get("/api/products/price-range", productController::getProductsByPriceRange);

        // 📦 Rutas CRUD principales de productos
        app.get("/api/products", productController::getAllProducts);           // GET - Obtener todos los productos
        app.post("/api/products", productController::createProduct);           // POST - Crear producto
        app.get("/api/products/{id}", productController::getProductById);      // GET - Obtener producto por ID
        app.put("/api/products/{id}", productController::updateProduct);       // PUT - Actualizar producto
        app.delete("/api/products/{id}", productController::deleteProduct);    // DELETE - Eliminar producto

        logger.info("✅ Rutas de productos configuradas correctamente");
        logAvailableRoutes();
    }

    /**
     * Log de todas las rutas disponibles para debugging
     */
    private void logAvailableRoutes() {
        logger.info("📋 Rutas de productos disponibles:");
        logger.info("  GET    /api/products/health           - Health check de productos");
        logger.info("  GET    /api/products                  - Obtener todos los productos");
        logger.info("  POST   /api/products                  - Crear nuevo producto");
        logger.info("  GET    /api/products/:id              - Obtener producto por ID");
        logger.info("  PUT    /api/products/:id              - Actualizar producto");
        logger.info("  DELETE /api/products/:id              - Eliminar producto");
        logger.info("  GET    /api/products/search?name=...  - Buscar productos por nombre");
        logger.info("  GET    /api/products/price-range?min=...&max=... - Buscar por rango de precio");
    }

    /**
     * Información de ejemplo de uso para productos
     */
    public static void logProductExamples(int port) {
        logger.info("💡 Ejemplos de uso para productos:");
        logger.info("📦 Crear producto:");
        logger.info("   curl -X POST http://localhost:{}/api/products \\", port);
        logger.info("   -H \"Content-Type: application/json\" \\");
        logger.info("   -d '{\"name\":\"Laptop\",\"price\":999.99,\"description\":\"Laptop gaming\"}'");

        logger.info("🔍 Buscar productos:");
        logger.info("   curl http://localhost:{}/api/products/search?name=laptop", port);
        logger.info("   curl http://localhost:{}/api/products/price-range?min=100&max=1000", port);

        logger.info("🔄 Actualizar producto:");
        logger.info("   curl -X PUT http://localhost:{}/api/products/1 \\", port);
        logger.info("   -H \"Content-Type: application/json\" \\");
        logger.info("   -d '{\"name\":\"Laptop Pro\",\"price\":1299.99}'");
    }
}