package org.devquality.services;

import org.devquality.web.dtos.products.request.CreateProductRequest;
import org.devquality.web.dtos.products.request.UpdateProductRequest;
import org.devquality.web.dtos.products.response.CreateProductResponse;
import org.devquality.web.dtos.products.response.DeletedBaseResponse;
import org.devquality.web.dtos.products.response.GetProductResponse;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface IProductService {

    CreateProductResponse createProduct(CreateProductRequest request) throws SQLException;

    List<GetProductResponse> getAllProducts() throws SQLException;

    GetProductResponse getProductById(Long id) throws SQLException;

    GetProductResponse updateProduct(Long id, UpdateProductRequest request) throws SQLException;

    DeletedBaseResponse deleteProductById(Long id) throws SQLException;

    List<GetProductResponse> searchProductsByName(String namePattern) throws SQLException;

    List<GetProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) throws SQLException;
}