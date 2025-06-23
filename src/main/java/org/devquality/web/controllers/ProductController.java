package org.devquality.web.controllers;

import org.devquality.services.IProductService;
import org.devquality.web.dtos.core.response.BaseResponse;
import org.devquality.web.dtos.products.request.CreateProductRequest;
import org.devquality.web.dtos.products.response.CreateProductResponse;
import org.devquality.web.dtos.products.response.DeletedBaseResponse;
import org.devquality.web.dtos.products.response.GetProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;

public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }


    public BaseResponse<CreateProductResponse> createProduct(CreateProductRequest product) throws SQLException {
        return null;
    }

    public BaseResponse<DeletedBaseResponse> deleteProduct(DeletedBaseResponse product) throws SQLException {
        return null;
    }

    public BaseResponse<ArrayList<GetProductResponse>> getAllProducts() throws SQLException {
        return null;
    }
    public BaseResponse<GetProductResponse> getProduct(GetProductResponse product) throws SQLException {
        return null;
    }

}
