package com.ecommerce.product_service.service;

import com.common_packages.common_packages.dto.ApiResponse;
import com.ecommerce.product_service.entity.Product;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ProductService {

    ApiResponse<Product> addProduct(Product product);

    ApiResponse<Product> updateProduct(Long id, Product product);

    @Nullable ApiResponse<Object> deleteProduct(Long id);

    ApiResponse<Product> getProduct(Long id);

    ApiResponse<List<Product>> getAllProducts();

    ApiResponse<List<Product>> searchProducts(String keyword);

    ApiResponse<List<Product>> getProductsByCategory(String category);

    ApiResponse<Product> updateStock(Long id, Integer quantityDelta);
}