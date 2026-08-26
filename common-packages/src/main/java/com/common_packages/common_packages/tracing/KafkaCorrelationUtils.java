package com.common_packages.common_packages.tracing;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class KafkaCorrelationUtils {

    private KafkaCorrelationUtils() {}

    public static <K, V> void injectCorrelationId(ProducerRecord<K, V> record) {
        String correlationId = MDC.get(CorrelationConstants.MDC_CORRELATION_ID_KEY);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }
        record.headers().remove(CorrelationConstants.CORRELATION_ID_HEADER);
        record.headers().add(new RecordHeader(CorrelationConstants.CORRELATION_ID_HEADER, correlationId.getBytes(StandardCharsets.UTF_8)));
    }

    public static void extractCorrelationId(ConsumerRecord<?, ?> record) {
        Header header = record.headers().lastHeader(CorrelationConstants.CORRELATION_ID_HEADER);
        String correlationId;
        if (header != null && header.value() != null) {
            correlationId = new String(header.value(), StandardCharsets.UTF_8);
        } else {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(CorrelationConstants.MDC_CORRELATION_ID_KEY, correlationId);
    }

    public static void clear() {
        MDC.remove(CorrelationConstants.MDC_CORRELATION_ID_KEY);
    }
}
