package com.ecommerce.order_service.client;

import com.ecommerce.order_service.client.dto.ProductResponse;
import com.ecommerce.order_service.dto.ServiceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductServiceClient {

    private final RestClient restClient;

    public ProductServiceClient(RestClient.Builder builder,
                                @Value("${services.product-service.url:http://localhost:8082}") String productServiceUrl) {
        this.restClient = builder
                .baseUrl(productServiceUrl)
                .build();
    }

    public ProductResponse getProduct(Long productId) {
        ServiceResponse<ProductResponse> response = restClient.get()
                .uri("/api/v1/products/{id}", productId)
                .retrieve()
                .body(new ParameterizedTypeReference<ServiceResponse<ProductResponse>>() {});

        if (response == null || response.getData() == null) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        return response.getData();
    }
}
