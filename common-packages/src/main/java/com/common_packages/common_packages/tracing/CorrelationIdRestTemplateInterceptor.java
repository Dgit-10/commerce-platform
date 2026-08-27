package com.common_packages.common_packages.tracing;

import jakarta.annotation.Nonnull;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(@Nonnull HttpRequest request, @Nonnull byte[] body, @Nonnull ClientHttpRequestExecution execution) throws IOException {
        String correlationId = MDC.get(CorrelationConstants.MDC_CORRELATION_ID_KEY);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }

        if (request.getHeaders().get(CorrelationConstants.CORRELATION_ID_HEADER) == null || (request.getHeaders().get(CorrelationConstants.CORRELATION_ID_HEADER) != null && request.getHeaders().get(CorrelationConstants.CORRELATION_ID_HEADER).isEmpty())) {
            request.getHeaders().add(CorrelationConstants.CORRELATION_ID_HEADER, correlationId);
        }

        return execution.execute(request, body);
    }
}
