package com.ecommerce.order_service.client;

import com.ecommerce.order_service.dto.ServiceResponse;
import com.ecommerce.order_service.client.dto.UserResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:8081")
                .build();
    }

    public UserResponse getUser(Long userId) {

        ServiceResponse<UserResponse> response = restClient.get()
                .uri("/api/v1/users/{id}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<
                        ServiceResponse<UserResponse>>() {});

        if (response == null || response.getData() == null) {
            throw new IllegalArgumentException(
                    "User not found with ID: " + userId);
        }

        return response.getData();
    }
}