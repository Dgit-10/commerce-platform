package com.ecommerce.order_service.client;

import com.ecommerce.order_service.client.dto.UserResponse;
import com.ecommerce.order_service.dto.ServiceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(RestClient.Builder builder,
                             @Value("${services.user-service.url:http://localhost:8081}") String userServiceUrl) {
        this.restClient = builder
                .baseUrl(userServiceUrl)
                .build();
    }

    public UserResponse getUser(Long userId, String authorizationHeader) {
        ServiceResponse<UserResponse> response = restClient.get()
                .uri("/api/v1/users/{id}", userId)
                .header("Authorization", authorizationHeader)
                .retrieve()
                .body(new ParameterizedTypeReference<ServiceResponse<UserResponse>>() {});

        if (response == null || response.getData() == null) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        return response.getData();
    }
}
