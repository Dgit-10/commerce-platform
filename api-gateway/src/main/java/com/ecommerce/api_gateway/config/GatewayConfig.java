package com.ecommerce.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${services.user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${services.product-service.url:http://localhost:8082}")
    private String productServiceUrl;

    @Value("${services.payment-service.url:http://localhost:8083}")
    private String paymentServiceUrl;

    @Value("${services.order-service.url:http://localhost:8084}")
    private String orderServiceUrl;

    @Value("${services.notification-service.url:http://localhost:8085}")
    private String notificationServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/api/v1/users/**", "/api/v1/auth/**")
                        .uri(userServiceUrl))

                .route("product-service", r -> r.path("/api/v1/products/**")
                        .uri(productServiceUrl))

                .route("order-service", r -> r.path("/api/v1/orders/**")
                        .uri(orderServiceUrl))

                .route("payment-service", r -> r.path("/api/v1/payments/**")
                        .uri(paymentServiceUrl))

                .route("notification-service", r -> r.path("/api/v1/notifications/**")
                        .uri(notificationServiceUrl))
                .build();
    }
}
