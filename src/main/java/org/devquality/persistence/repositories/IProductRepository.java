package org.devquality.persistence.repositories;

import org.devquality.persistence.entites.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface IProductRepository {
    Product save(String name, BigDecimal price, String description) throws SQLException;

    List<Product> findAll() throws SQLException;

    Product findById(Long id) throws SQLException;

    Product update(Long id, String name, BigDecimal price, String description) throws SQLException;

    boolean deleteById(Long id) throws SQLException;

    List<Product> findByNameContaining(String namePattern) throws SQLException;

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) throws SQLException;
}
