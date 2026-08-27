package com.ecommerce.order_service.config;

import com.common_packages.common_packages.tracing.CorrelationConstants;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Configuration
public class ClientConfig {

    @Bean
    public ClientHttpRequestInterceptor correlationIdInterceptor() {
        return (request, body, execution) -> {
            String correlationId = MDC.get(CorrelationConstants.MDC_CORRELATION_ID_KEY);
            if (!StringUtils.hasText(correlationId)) {
                correlationId = UUID.randomUUID().toString();
            }
            if (request.getHeaders().get(CorrelationConstants.CORRELATION_ID_HEADER) == null || (request.getHeaders().get(CorrelationConstants.CORRELATION_ID_HEADER) != null && request.getHeaders().get(CorrelationConstants.CORRELATION_ID_HEADER).isEmpty())) {
                request.getHeaders().add(CorrelationConstants.CORRELATION_ID_HEADER, correlationId);
            }
            return execution.execute(request, body);
        };
    }

    @Bean
    public RestClient.Builder restClientBuilder(ClientHttpRequestInterceptor correlationIdInterceptor) {
        return RestClient.builder()
                .requestInterceptor(correlationIdInterceptor);
    }
}
