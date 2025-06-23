package org.devquality.persistence.repositories.impl;

import org.devquality.config.DatabaseConfig;
import org.devquality.persistence.entites.Product;
import org.devquality.persistence.repositories.IProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository implements IProductRepository {
    private static final Logger logger = LoggerFactory.getLogger(ProductRepository.class);
    private final DatabaseConfig databaseConfig;

    // SQL Queries
    private static final String INSERT_PRODUCT =
            "INSERT INTO products (name, price, description) VALUES (?, ?, ?) RETURNING id, created_at, updated_at";

    private static final String SELECT_ALL_PRODUCTS =
            "SELECT id, name, price, description, created_at, updated_at FROM products ORDER BY created_at DESC";

    private static final String SELECT_PRODUCT_BY_ID =
            "SELECT id, name, price, description, created_at, updated_at FROM products WHERE id = ?";

    private static final String UPDATE_PRODUCT =
            "UPDATE products SET name = ?, price = ?, description = ? WHERE id = ? RETURNING updated_at";

    private static final String DELETE_PRODUCT_BY_ID =
            "DELETE FROM products WHERE id = ?";

    private static final String COUNT_PRODUCTS =
            "SELECT COUNT(*) FROM products";

    private static final String SEARCH_PRODUCTS_BY_NAME =
            "SELECT id, name, price, description, created_at, updated_at FROM products " +
                    "WHERE LOWER(name) LIKE LOWER(?) ORDER BY name";

    private static final String SELECT_PRODUCTS_BY_PRICE_RANGE =
            "SELECT id, name, price, description, created_at, updated_at FROM products " +
                    "WHERE price BETWEEN ? AND ? ORDER BY price";

    public ProductRepository(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    /**
     * Guarda un nuevo producto en la base de datos
     */
    @Override
    public Product save(String name, BigDecimal price, String description) throws SQLException {
        logger.debug("💾 Guardando producto: {}", name);

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_PRODUCT)) {

            stmt.setString(1, name);
            stmt.setBigDecimal(2, price);
            stmt.setString(3, description);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product savedProduct = new Product();
                    savedProduct.setId(rs.getLong("id"));
                    savedProduct.setName(name);
                    savedProduct.setPrice(price);
                    savedProduct.setDescription(description);
                    savedProduct.setCreatedAt(String.valueOf(rs.getTimestamp("created_at").toLocalDateTime()));
                    savedProduct.setUpdatedAt(String.valueOf(rs.getTimestamp("updated_at").toLocalDateTime()));

                    logger.info("✅ Producto guardado con ID: {}", savedProduct.getId());
                    return savedProduct;
                } else {
                    throw new SQLException("No se pudo obtener el ID del producto creado");
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Error al guardar producto: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene todos los productos
     */
    @Override
    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        logger.debug("🔍 Obteniendo todos los productos");

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_PRODUCTS);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }

            logger.info("✅ {} productos obtenidos", products.size());
            return products;

        } catch (SQLException e) {
            logger.error("❌ Error al obtener productos: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Busca un producto por ID
     */
    @Override
    public Product findById(Long id) throws SQLException {
        logger.debug("🔍 Buscando producto por ID: {}", id);

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_PRODUCT_BY_ID)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product product = mapResultSetToProduct(rs);
                    logger.debug("✅ Producto encontrado: {}", product.getName());
                    return product;
                } else {
                    logger.debug("❌ Producto con ID {} no encontrado", id);
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Error al buscar producto por ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Actualiza un producto existente
     */
    @Override
    public Product update(Long id, String name, BigDecimal price, String description) throws SQLException {
        logger.debug("🔄 Actualizando producto ID: {}", id);

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_PRODUCT)) {

            stmt.setString(1, name);
            stmt.setBigDecimal(2, price);
            stmt.setString(3, description);
            stmt.setLong(4, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Obtener el producto actualizado
                    Product updatedProduct = findById(id);
                    logger.info("✅ Producto actualizado: {}", id);
                    return updatedProduct;
                } else {
                    logger.warn("❌ Producto con ID {} no encontrado para actualizar", id);
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Error al actualizar producto {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Elimina un producto por ID
     */
    @Override
    public boolean deleteById(Long id) throws SQLException {
        logger.debug("🗑️ Eliminando producto ID: {}", id);

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_PRODUCT_BY_ID)) {

            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();

            boolean deleted = rowsAffected > 0;
            if (deleted) {
                logger.info("✅ Producto eliminado: {}", id);
            } else {
                logger.warn("❌ Producto con ID {} no encontrado para eliminar", id);
            }

            return deleted;

        } catch (SQLException e) {
            logger.error("❌ Error al eliminar producto {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Cuenta el total de productos
     */
    public long count() throws SQLException {
        logger.debug("📊 Contando total de productos");

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_PRODUCTS);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                long count = rs.getLong(1);
                logger.debug("📊 Total de productos: {}", count);
                return count;
            }
            return 0;

        } catch (SQLException e) {
            logger.error("❌ Error al contar productos: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Busca productos por nombre (búsqueda parcial)
     */
    @Override
    public List<Product> findByNameContaining(String namePattern) throws SQLException {
        List<Product> products = new ArrayList<>();
        logger.debug("🔍 Buscando productos por nombre: {}", namePattern);

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_PRODUCTS_BY_NAME)) {

            stmt.setString(1, "%" + namePattern + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }

            logger.info("✅ {} productos encontrados con nombre '{}'", products.size(), namePattern);
            return products;

        } catch (SQLException e) {
            logger.error("❌ Error al buscar productos por nombre: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Busca productos en un rango de precios
     */
    @Override
    public List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) throws SQLException {
        List<Product> products = new ArrayList<>();
        logger.debug("🔍 Buscando productos entre {} y {}", minPrice, maxPrice);

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_PRODUCTS_BY_PRICE_RANGE)) {

            stmt.setBigDecimal(1, minPrice);
            stmt.setBigDecimal(2, maxPrice);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }

            logger.info("✅ {} productos encontrados en rango de precio", products.size());
            return products;

        } catch (SQLException e) {
            logger.error("❌ Error al buscar productos por rango de precio: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Mapea un ResultSet a un objeto Product
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setDescription(rs.getString("description"));
        product.setCreatedAt(String.valueOf(rs.getTimestamp("created_at").toLocalDateTime()));
        product.setUpdatedAt(String.valueOf(rs.getTimestamp("updated_at").toLocalDateTime()));
        return product;
    }
}