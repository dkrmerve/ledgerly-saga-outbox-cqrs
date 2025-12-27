package com.example.ledgerly.infra.kafka;

import com.example.ledgerly.observability.LogFields;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class KafkaMdcInterceptor implements RecordInterceptor<String, String> {

    @Override
    public ConsumerRecord<String, String> intercept(ConsumerRecord<String, String> record) {
        putHeader(record, KafkaHeaders.CORRELATION_ID, LogFields.CORRELATION_ID);
        putHeader(record, KafkaHeaders.IDEMPOTENCY_KEY, LogFields.IDEMPOTENCY_KEY);
        putHeader(record, KafkaHeaders.EVENT_ID, LogFields.EVENT_ID);
        putHeader(record, KafkaHeaders.ORDER_ID, LogFields.ORDER_ID);

        MDC.put(LogFields.KAFKA_TOPIC, record.topic());
        MDC.put(LogFields.KAFKA_PARTITION, String.valueOf(record.partition()));
        MDC.put(LogFields.KAFKA_OFFSET, String.valueOf(record.offset()));
        return record;
    }

    private void putHeader(ConsumerRecord<String, String> record, String headerKey, String mdcKey) {
        var h = record.headers().lastHeader(headerKey);
        if (h != null) {
            MDC.put(mdcKey, new String(h.value(), StandardCharsets.UTF_8));
        }
    }
}
