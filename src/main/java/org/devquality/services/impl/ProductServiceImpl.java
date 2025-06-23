package org.devquality.services.impl;

import org.devquality.services.IProductService;
import org.devquality.web.dtos.core.response.BaseResponse;
import org.devquality.web.dtos.products.request.CreateProductRequest;
import org.devquality.web.dtos.products.response.CreateProductResponse;
import org.devquality.web.dtos.products.response.DeletedBaseResponse;
import org.devquality.web.dtos.products.response.GetProductResponse;

import java.sql.SQLException;
import java.util.ArrayList;

public class ProductServiceImpl implements IProductService {

    @Override
    public BaseResponse<CreateProductResponse> createProduct(CreateProductRequest product) throws SQLException {
        return null;
    }

    @Override
    public BaseResponse<ArrayList<GetProductResponse>> getAllProducts(CreateProductRequest product) throws SQLException {
        return null;
    }

    @Override
    public BaseResponse<GetProductResponse> getProductById(Long id) throws SQLException {
        return null;
    }

    @Override
    public BaseResponse<DeletedBaseResponse> deleteProductById(Long id) {
        return null;
    }
}
