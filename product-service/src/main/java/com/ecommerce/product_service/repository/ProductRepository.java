package com.ecommerce.product_service.repository;

import com.ecommerce.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryAndActiveTrue(String category);

    List<Product> findByBrandAndActiveTrue(String brand);

    List<Product> findByProductNameContainingIgnoreCaseAndActiveTrue(String productName);

    List<Product> findByActiveTrue();

    boolean existsBySku(String sku);
}