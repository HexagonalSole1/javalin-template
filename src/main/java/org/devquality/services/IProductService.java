package org.devquality.services;

import org.devquality.persistence.entites.Product;
import org.devquality.web.dtos.core.response.BaseResponse;
import org.devquality.web.dtos.products.response.CreateProductResponse;
import org.devquality.web.dtos.products.request.CreateProductRequest;
import org.devquality.web.dtos.products.response.DeletedBaseResponse;
import org.devquality.web.dtos.products.response.GetProductResponse;

import java.sql.SQLException;
import java.util.ArrayList;

public interface IProductService {

    BaseResponse<CreateProductResponse> createProduct(CreateProductRequest product) throws SQLException;
    BaseResponse<ArrayList<GetProductResponse>> getAllProducts(CreateProductRequest product) throws SQLException;
    BaseResponse<GetProductResponse> getProductById(Long id) throws SQLException;

    BaseResponse<DeletedBaseResponse> deleteProductById(Long id);


}
