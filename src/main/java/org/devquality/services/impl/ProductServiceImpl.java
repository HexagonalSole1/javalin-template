package org.devquality.services.impl;

import org.devquality.persistence.entites.Product;
import org.devquality.persistence.repositories.IProductRepository;
import org.devquality.services.IProductService;
import org.devquality.web.dtos.core.response.BaseResponse;
import org.devquality.web.dtos.products.request.CreateProductRequest;
import org.devquality.web.dtos.products.request.UpdateProductRequest;
import org.devquality.web.dtos.products.response.CreateProductResponse;
import org.devquality.web.dtos.products.response.DeletedBaseResponse;
import org.devquality.web.dtos.products.response.GetProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductServiceImpl implements IProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final IProductRepository productRepository;

    public ProductServiceImpl(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public CreateProductResponse createProduct(CreateProductRequest request) throws SQLException {
        logger.debug("📝 Procesando creación de producto: {}", request.getName());

        // Validación adicional a nivel de servicio
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (request.getPrice() == null) {
            throw new IllegalArgumentException("El precio es obligatorio");
        }

        if (request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }

        // Guardar producto
        Product savedProduct = productRepository.save(
                request.getName().trim(),
                request.getPrice(),
                request.getDescription() != null ? request.getDescription().trim() : null
        );

        // Crear respuesta
        CreateProductResponse response = CreateProductResponse.builder()
                .id(savedProduct.getId())
                .name(savedProduct.getName())
                .price(savedProduct.getPrice())
                .description(savedProduct.getDescription())
                .createdAt(savedProduct.getCreatedAt())
                .build();

        logger.info("✅ Producto creado exitosamente: {} - ID: {}", savedProduct.getName(), savedProduct.getId());
        return response;
    }

    @Override
    public List<GetProductResponse> getAllProducts() throws SQLException {
        logger.debug("📋 Obteniendo todos los productos desde el servicio");

        List<Product> products = productRepository.findAll();
        List<GetProductResponse> responses = new ArrayList<>();

        for (Product product : products) {
            GetProductResponse response = GetProductResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .description(product.getDescription())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .build();
            responses.add(response);
        }

        logger.info("✅ {} productos obtenidos desde el servicio", responses.size());
        return responses;
    }

    @Override
    public GetProductResponse getProductById(Long id) throws SQLException {
        logger.debug("🔍 Obteniendo producto por ID: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un número positivo");
        }

        Product product = productRepository.findById(id);

        if (product == null) {
            logger.warn("❌ Producto con ID {} no encontrado", id);
            return null;
        }

        GetProductResponse response = GetProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();

        logger.info("✅ Producto encontrado: {} - {}", product.getId(), product.getName());
        return response;
    }

    @Override
    public GetProductResponse updateProduct(Long id, UpdateProductRequest request) throws SQLException {
        logger.debug("🔄 Actualizando producto ID: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un número positivo");
        }

        // Verificar que el producto existe
        Product existingProduct = productRepository.findById(id);
        if (existingProduct == null) {
            logger.warn("❌ Producto con ID {} no encontrado para actualizar", id);
            return null;
        }

        // Usar valores existentes si no se proporcionan nuevos
        String newName = request.getName() != null ? request.getName().trim() : existingProduct.getName();
        java.math.BigDecimal newPrice = request.getPrice() != null ? request.getPrice() : existingProduct.getPrice();
        String newDescription = request.getDescription() != null ? request.getDescription().trim() : existingProduct.getDescription();

        // Validaciones
        if (newPrice.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }

        // Actualizar producto
        Product updatedProduct = productRepository.update(id, newName, newPrice, newDescription);

        if (updatedProduct == null) {
            throw new SQLException("No se pudo actualizar el producto");
        }

        GetProductResponse response = GetProductResponse.builder()
                .id(updatedProduct.getId())
                .name(updatedProduct.getName())
                .price(updatedProduct.getPrice())
                .description(updatedProduct.getDescription())
                .createdAt(updatedProduct.getCreatedAt())
                .updatedAt(updatedProduct.getUpdatedAt())
                .build();

        logger.info("✅ Producto actualizado exitosamente: {} - ID: {}", updatedProduct.getName(), updatedProduct.getId());
        return response;
    }

    @Override
    public DeletedBaseResponse deleteProductById(Long id) throws SQLException {
        logger.debug("🗑️ Eliminando producto ID: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un número positivo");
        }

        // Verificar que el producto existe antes de eliminar
        Product existingProduct = productRepository.findById(id);
        if (existingProduct == null) {
            logger.warn("❌ Producto con ID {} no encontrado para eliminar", id);
            return DeletedBaseResponse.failure(id, "Producto no encontrado");
        }

        // Eliminar producto
        boolean deleted = productRepository.deleteById(id);

        if (deleted) {
            logger.info("✅ Producto eliminado exitosamente: {} - ID: {}", existingProduct.getName(), id);
            return DeletedBaseResponse.success(id, "Producto eliminado correctamente");
        } else {
            logger.error("❌ No se pudo eliminar el producto con ID: {}", id);
            return DeletedBaseResponse.failure(id, "No se pudo eliminar el producto");
        }
    }

    @Override
    public List<GetProductResponse> searchProductsByName(String namePattern) throws SQLException {
        logger.debug("🔍 Buscando productos por nombre: {}", namePattern);

        if (namePattern == null || namePattern.trim().isEmpty()) {
            throw new IllegalArgumentException("El patrón de búsqueda no puede estar vacío");
        }

        List<Product> products = productRepository.findByNameContaining(namePattern.trim());
        List<GetProductResponse> responses = new ArrayList<>();

        for (Product product : products) {
            GetProductResponse response = GetProductResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .description(product.getDescription())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .build();
            responses.add(response);
        }

        logger.info("✅ {} productos encontrados con patrón '{}'", responses.size(), namePattern);
        return responses;
    }

    @Override
    public List<GetProductResponse> getProductsByPriceRange(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) throws SQLException {
        logger.debug("🔍 Buscando productos entre {} y {}", minPrice, maxPrice);

        if (minPrice == null || maxPrice == null) {
            throw new IllegalArgumentException("Los precios mínimo y máximo son obligatorios");
        }

        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor al precio máximo");
        }

        if (minPrice.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio mínimo debe ser mayor o igual a 0");
        }

        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        List<GetProductResponse> responses = new ArrayList<>();

        for (Product product : products) {
            GetProductResponse response = GetProductResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .description(product.getDescription())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .build();
            responses.add(response);
        }

        logger.info("✅ {} productos encontrados en rango de precio", responses.size());
        return responses;
    }
}