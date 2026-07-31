package com.ecommerce.product_service.service.impl;

import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.repository.ProductRepository;
import com.ecommerce.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ApiResponse<Product> addProduct(Product product) {
        log.info("Attempting to create new product with SKU: {}", product.getSku());

        if (productRepository.existsBySku(product.getSku())) {
            log.error("Product creation failed. Duplicate SKU: {}", product.getSku());
            throw new DuplicateSKUException("Product with SKU '" + product.getSku() + "' already exists.");
        }

        if (product.getStockQuantity() != null && product.getStockQuantity() < 0) {
            log.error("Product creation failed. Invalid initial stock: {}", product.getStockQuantity());
            throw new InvalidStockException("Initial stock quantity cannot be negative.");
        }

        product.setActive(true);
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return ResponseBuilder.success(savedProduct, "Product added successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Product> updateProduct(Long id, Product productDetails) {
        log.info("Attempting to update product ID: {}", id);

        Product existingProduct = findActiveProductEntity(id);

        if (!existingProduct.getSku().equals(productDetails.getSku()) && productRepository.existsBySku(productDetails.getSku())) {
            log.error("Product update failed. SKU '{}' already exists.", productDetails.getSku());
            throw new DuplicateSKUException("Product with SKU '" + productDetails.getSku() + "' already exists.");
        }

        existingProduct.setSku(productDetails.getSku());
        existingProduct.setProductName(productDetails.getProductName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setBrand(productDetails.getBrand());
        existingProduct.setCategory(productDetails.getCategory());
        existingProduct.setPrice(productDetails.getPrice());

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully for ID: {}", id);

        return ResponseBuilder.success(updatedProduct, "Product updated successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteProduct(Long id) {
        log.info("Executing soft delete for product ID: {}", id);

        Product product = findActiveProductEntity(id);

        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft-deleted successfully for ID: {}", id);

        return ResponseBuilder.success("Product deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Product> getProduct(Long id) {
        log.info("Fetching details for product ID: {}", id);
        Product product = findActiveProductEntity(id);
        return ResponseBuilder.success(product, "Product retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<Product>> getAllProducts() {
        log.info("Fetching all active products");
        List<Product> products = productRepository.findByActiveTrue();
        return ResponseBuilder.success(products, "All active products retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<Product>> searchProducts(String keyword) {
        log.info("Searching products with keyword: '{}'", keyword);
        List<Product> products = productRepository.findByProductNameContainingIgnoreCaseAndActiveTrue(keyword);
        return ResponseBuilder.success(products, "Product search completed successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<Product>> getProductsByCategory(String category) {
        log.info("Fetching active products under category: '{}'", category);
        List<Product> products = productRepository.findByCategoryAndActiveTrue(category);
        return ResponseBuilder.success(products, "Category products retrieved successfully");
    }

    @Override
    @Transactional
    public ApiResponse<Product> updateStock(Long id, Integer quantityDelta) {
        log.info("Updating stock for product ID: {} with delta: {}", id, quantityDelta);

        Product product = findActiveProductEntity(id);
        int updatedStock = product.getStockQuantity() + quantityDelta;

        if (updatedStock < 0) {
            log.error("Stock update failed for product ID: {}. Current stock: {}, requested delta: {}",
                    id, product.getStockQuantity(), quantityDelta);
            throw new InvalidStockException("Insufficient stock available. Current stock: " + product.getStockQuantity());
        }

        product.setStockQuantity(updatedStock);
        Product savedProduct = productRepository.save(product);
        log.info("Stock updated successfully for product ID: {}. New total stock: {}", id, updatedStock);

        return ResponseBuilder.success(savedProduct, "Product stock updated successfully");
    }

    private Product findActiveProductEntity(Long id) {
        return productRepository.findById(id)
                .filter(Product::getActive)
                .orElseThrow(() -> {
                    log.error("Product lookup failed for ID: {} (Product not found or inactive)", id);
                    return new ProductNotFoundException("Product not found with ID: " + id);
                });
    }
}