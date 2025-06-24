package org.devquality.web.controllers;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.devquality.services.IProductService;
import org.devquality.web.dtos.core.response.BaseResponse;
import org.devquality.web.dtos.core.response.ResponseMetadata;
import org.devquality.web.dtos.products.request.CreateProductRequest;
import org.devquality.web.dtos.products.request.UpdateProductRequest;
import org.devquality.web.dtos.products.response.CreateProductResponse;
import org.devquality.web.dtos.products.response.DeletedBaseResponse;
import org.devquality.web.dtos.products.response.GetProductResponse;
import org.devquality.web.middleware.BeanValidationMiddleware;
import org.devquality.web.validators.groups.ValidationGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    /**
     * POST /api/products - Crear nuevo producto
     */
    public void createProduct(Context ctx) {
        CreateProductRequest request = BeanValidationMiddleware.validateRequest(
                ctx,
                CreateProductRequest.class,
                ValidationGroups.Create.class
        );

        if (request == null) return;

        try {
            CreateProductResponse productResponse = productService.createProduct(request);

            ctx.status(HttpStatus.CREATED).json(
                    BaseResponse.success(productResponse, "Producto creado correctamente")
            );

        } catch (SQLException e) {
            BeanValidationMiddleware.handleDatabaseError(ctx, e);
        } catch (IllegalArgumentException e) {
            logger.warn("❌ Argumento inválido: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST).json(
                    BaseResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            BeanValidationMiddleware.handleError(ctx, "Error al crear producto", e);
        }
    }

    /**
     * GET /api/products - Obtener todos los productos
     */
    public void getAllProducts(Context ctx) {
        try {
            List<GetProductResponse> products = productService.getAllProducts();

            ResponseMetadata metadata = ResponseMetadata.builder()
                    .type("PRODUCT_LIST")
                    .totalElements((long) products.size())
                    .build();

            ctx.status(HttpStatus.OK).json(
                    BaseResponse.success(products, "Productos obtenidos correctamente", metadata)
            );

        } catch (Exception e) {
            BeanValidationMiddleware.handleError(ctx, "Error al obtener productos", e);
        }
    }

    /**
     * GET /api/products/{id} - Obtener producto por ID
     */
    public void getProductById(Context ctx) {
        Long productId = BeanValidationMiddleware.validateId(ctx, "id");
        if (productId == null) return;

        try {
            GetProductResponse product = productService.getProductById(productId);

            if (product == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(
                        BaseResponse.error("Producto no encontrado")
                );
                return;
            }

            ctx.status(HttpStatus.OK).json(
                    BaseResponse.success(product, "Producto encontrado")
            );

        } catch (IllegalArgumentException e) {
            logger.warn("❌ Argumento inválido: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST).json(
                    BaseResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            BeanValidationMiddleware.handleError(ctx, "Error al obtener producto", e);
        }
    }

    /**
     * PUT /api/products/{id} - Actualizar producto
     */
    public void updateProduct(Context ctx) {
        Long productId = BeanValidationMiddleware.validateId(ctx, "id");
        if (productId == null) return;

        UpdateProductRequest request = BeanValidationMiddleware.validateRequest(
                ctx,
                UpdateProductRequest.class,
                ValidationGroups.Update.class
        );

        if (request == null) return;

        try {
            // Asegurar que el ID del path coincida con el del body (si existe)
            if (request.getId() != null && !request.getId().equals(productId)) {
                ctx.status(HttpStatus.BAD_REQUEST).json(
                        BaseResponse.error("El ID del path no coincide con el ID del body")
                );
                return;
            }

            GetProductResponse updatedProduct = productService.updateProduct(productId, request);

            if (updatedProduct == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(
                        BaseResponse.error("Producto no encontrado")
                );
                return;
            }

            ctx.status(HttpStatus.OK).json(
                    BaseResponse.success(updatedProduct, "Producto actualizado correctamente")
            );

        } catch (SQLException e) {
            BeanValidationMiddleware.handleDatabaseError(ctx, e);
        } catch (IllegalArgumentException e) {
            logger.warn("❌ Argumento inválido: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST).json(
                    BaseResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            BeanValidationMiddleware.handleError(ctx, "Error al actualizar producto", e);
        }
    }

    /**
     * DELETE /api/products/{id} - Eliminar producto
     */
    public void deleteProduct(Context ctx) {
        Long productId = BeanValidationMiddleware.validateId(ctx, "id");
        if (productId == null) return;

        try {
            DeletedBaseResponse result = productService.deleteProductById(productId);

            if (!result.isSuccess()) {
                ctx.status(HttpStatus.NOT_FOUND).json(
                        BaseResponse.error(result.getMessage())
                );
                return;
            }

            ctx.status(HttpStatus.OK).json(
                    BaseResponse.success(result, "Producto eliminado correctamente")
            );

        } catch (IllegalArgumentException e) {
            logger.warn("❌ Argumento inválido: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST).json(
                    BaseResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            BeanValidationMiddleware.handleError(ctx, "Error al eliminar producto", e);
        }
    }

    /**
     * GET /api/products/search?name={pattern} - Buscar productos por nombre
     */
    public void searchProductsByName(Context ctx) {
        String namePattern = ctx.queryParam("name");

        if (namePattern == null || namePattern.trim().isEmpty()) {
            ctx.status(HttpStatus.BAD_REQUEST).json(
                    BaseResponse.error("El parámetro 'name' es obligatorio para la búsqueda")
            );
            return;
        }

        try {
            List<GetProductResponse> products = productService.searchProductsByName(namePattern);

            ResponseMetadata metadata = ResponseMetadata.builder()
                    .type("PRODUCT_SEARCH")
                    .totalElements((long) products.size())
                    .build();

            ctx.status(HttpStatus.OK).json(
                    BaseResponse.success(products,
                            String.format("Se encontraron %d productos con el patrón '%s'", products.size(), namePattern),
                            metadata)
            );

        } catch (IllegalArgumentException e) {
            logger.warn("❌ Argumento inválido: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST).json(
                    BaseResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            BeanValidationMiddleware.handleError(ctx, "Error al buscar productos", e);
        }
    }

    /**
     * GET /api/products/price-range?min={minPrice}&max={maxPrice} - Buscar productos por rango de precios
     */
    public void getProductsByPriceRange(Context ctx) {
        String minPriceStr = ctx.queryParam("min");
        String maxPriceStr = ctx.queryParam("max");

        if (minPriceStr == null || maxPriceStr == null) {
            ctx.status(HttpStatus.BAD_REQUEST).json(
                    BaseResponse.error("Los parámetros 'min' y 'max' son obligatorios")
            );
            return;
        }

        try {
            BigDecimal minPrice = new BigDecimal(minPriceStr);
            BigDecimal maxPrice = new BigDecimal(maxPriceStr);

            List<GetProductResponse> products = productService.getProductsByPriceRange(minPrice, maxPrice);

            ResponseMetadata metadata = ResponseMetadata.builder()
                    .type("PRODUCT_PRICE_RANGE")
                    .totalElements((long) products.size())
                    .build();

            ctx.status(HttpStatus.OK).json(
                    BaseResponse.success(products,
                            String.format("Se encontraron %d productos entre $%s y $%s", products.size(), minPrice, maxPrice),
                            metadata)
            );

        } catch (NumberFormatException e) {
            logger.warn("❌ Formato de precio inválido: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST).json(
                    BaseResponse.error("Los precios deben ser números válidos")
            );
        } catch (IllegalArgumentException e) {
            logger.warn("❌ Argumento inválido: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST).json(
                    BaseResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            BeanValidationMiddleware.handleError(ctx, "Error al buscar productos por rango de precio", e);
        }
    }

    /**
     * GET /api/products/health - Health check específico para productos
     */
    public void healthCheck(Context ctx) {
        var healthData = java.util.Map.of(
                "status", "UP",
                "service", "ProductService",
                "version", "1.0.0",
                "features", java.util.List.of(
                        "CRUD operations",
                        "Search by name",
                        "Filter by price range"
                )
        );

        ctx.status(HttpStatus.OK).json(
                BaseResponse.success(healthData, "Servicio de productos funcionando correctamente")
        );
    }
}