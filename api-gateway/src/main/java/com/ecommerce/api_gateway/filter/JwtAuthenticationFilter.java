package com.ecommerce.api_gateway.filter;

import com.ecommerce.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/v1/users/register",
            "/api/v1/users/login",
            "/actuator/health",
            "/actuator/info"
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod() != null ? request.getMethod().name() : "";

        // Ensure correlation ID is present or generate new
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }

        // Add correlation ID to response headers
        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);

        // Mutator builder for downstream request
        ServerHttpRequest.Builder requestBuilder = request.mutate()
                .header(CORRELATION_ID_HEADER, correlationId);

        // Allow public endpoints
        if (isOpenEndpoint(path, method)) {
            return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
        }

        // Check Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
        }

        try {
            Claims claims = jwtUtil.getAllClaimsFromToken(token);
            String userId = claims.getSubject() != null ? claims.getSubject() : String.valueOf(claims.get("userId"));
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            if (StringUtils.hasText(userId)) {
                requestBuilder.header("X-User-Id", userId);
            }
            if (StringUtils.hasText(email)) {
                requestBuilder.header("X-User-Email", email);
            }
            if (StringUtils.hasText(role)) {
                requestBuilder.header("X-User-Role", role);
            }
        } catch (Exception e) {
            return onError(exchange, "Failed to parse JWT claims", HttpStatus.UNAUTHORIZED);
        }

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    private boolean isOpenEndpoint(String path, String method) {
        if (path.startsWith("/api/v1/products") && "GET".equalsIgnoreCase(method)) {
            return true;
        }
        return OPEN_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errMessage, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String jsonError = String.format("{\"success\":false,\"message\":\"%s\",\"timestamp\":\"%s\"}",
                errMessage, java.time.LocalDateTime.now());
        byte[] bytes = jsonError.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
