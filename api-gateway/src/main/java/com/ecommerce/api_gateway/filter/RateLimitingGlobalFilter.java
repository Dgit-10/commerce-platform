package com.ecommerce.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingGlobalFilter.class);

    // Limit configuration: 100 requests per minute per IP
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long WINDOW_SIZE_MS = 60_000L;

    private static class TokenBucket {
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());

        boolean allowRequest(int maxRequests, long windowMs) {
            long now = System.currentTimeMillis();
            long start = windowStart.get();

            if (now - start > windowMs) {
                if (windowStart.compareAndSet(start, now)) {
                    count.set(0);
                }
            }

            return count.incrementAndGet() <= maxRequests;
        }
    }

    private final Map<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientKey = resolveClientKey(exchange.getRequest());

        TokenBucket bucket = clientBuckets.computeIfAbsent(clientKey, k -> new TokenBucket());

        if (!bucket.allowRequest(MAX_REQUESTS_PER_MINUTE, WINDOW_SIZE_MS)) {
            log.warn("Rate limit exceeded for client: {}", clientKey);
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            response.getHeaders().set("Retry-After", "60");

            String errorBody = String.format(
                    "{\"success\":false,\"message\":\"Rate limit exceeded. Maximum %d requests per minute allowed.\",\"timestamp\":\"%s\"}",
                    MAX_REQUESTS_PER_MINUTE, LocalDateTime.now()
            );

            byte[] bytes = errorBody.getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        }

        return chain.filter(exchange);
    }

    private String resolveClientKey(ServerHttpRequest request) {
        // Prefer authenticated user if present in headers, else client IP
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isBlank()) {
            return "user:" + userId;
        }

        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return "ip:" + remoteAddress.getAddress().getHostAddress();
        }

        return "anonymous";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }
}
