package com.example.ledgerly.application.saga;

import com.example.ledgerly.application.inbox.InboxService;
import com.example.ledgerly.application.saga.steps.PaymentAuthorizeStep;
import com.example.ledgerly.application.saga.steps.PaymentCaptureStep;
import com.example.ledgerly.application.saga.steps.PaymentVoidStep;
import com.example.ledgerly.application.saga.steps.StockReserveStep;
import com.example.ledgerly.domain.event.EventTypes;
import com.example.ledgerly.domain.event.SagaEvent;
import com.example.ledgerly.infra.kafka.KafkaHeaders;
import com.example.ledgerly.infra.redis.RedisLockService;
import com.example.ledgerly.observability.LogFields;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class OrderSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaOrchestrator.class);
    private static final String CONSUMER_NAME = "saga-orchestrator";

    private final InboxService inboxService;
    private final RedisLockService lockService;

    private final PaymentAuthorizeStep authorizeStep;
    private final StockReserveStep stockReserveStep;
    private final PaymentCaptureStep captureStep;
    private final PaymentVoidStep voidStep;

    private final ObjectMapper om;

    public OrderSagaOrchestrator(
            InboxService inboxService,
            RedisLockService lockService,
            PaymentAuthorizeStep authorizeStep,
            StockReserveStep stockReserveStep,
            PaymentCaptureStep captureStep,
            PaymentVoidStep voidStep,
            ObjectMapper om
    ) {
        this.inboxService = inboxService;
        this.lockService = lockService;
        this.authorizeStep = authorizeStep;
        this.stockReserveStep = stockReserveStep;
        this.captureStep = captureStep;
        this.voidStep = voidStep;
        this.om = om;
    }

    @KafkaListener(
            topics = "#{T(com.example.ledgerly.infra.kafka.KafkaTopics).topicSagaCommands()}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onSagaCommand(ConsumerRecord<String, String> record, Acknowledgment ack) throws Exception {

        String eventIdStr = header(record, KafkaHeaders.EVENT_ID);
        UUID eventId = UUID.fromString(eventIdStr);

        String correlationStr = header(record, KafkaHeaders.CORRELATION_ID);
        UUID correlationId = UUID.fromString(correlationStr);

        String idempotencyKey = header(record, KafkaHeaders.IDEMPOTENCY_KEY);

        MDC.put(LogFields.CORRELATION_ID, correlationStr);

        try {
            if (!inboxService.claim(CONSUMER_NAME, eventId)) {
                log.info("Duplicate saga command dropped eventId={}", eventId);
                ack.acknowledge();
                return;
            }

            SagaEvent event = om.readValue(record.value(), SagaEvent.class);
            event.correlationId = correlationId;
            event.idempotencyKey = idempotencyKey;

            String lockKey = "order:" + event.orderId;
            String token = lockService.tryLock(lockKey);
            if (token == null) {
                throw new IllegalStateException("Could not acquire order lock");
            }

            try {
                MDC.put(LogFields.SAGA_STEP, event.eventType);

                switch (event.eventType) {
                    case EventTypes.PAYMENT_AUTH_REQUESTED:
                        authorizeStep.handle(event);
                        break;
                    case EventTypes.STOCK_RESERVE_REQUESTED:
                        stockReserveStep.handle(event);
                        break;
                    case EventTypes.PAYMENT_CAPTURE_REQUESTED:
                        captureStep.handle(event);
                        break;
                    case EventTypes.PAYMENT_VOID_REQUESTED:
                        voidStep.handle(event);
                        break;
                    default:
                        log.warn("Unknown saga eventType: {}", event.eventType);
                        break;
                }

                ack.acknowledge();
            } finally {
                lockService.unlock(lockKey, token);
                MDC.remove(LogFields.SAGA_STEP);
            }
        } finally {
            MDC.remove(LogFields.CORRELATION_ID);
        }
    }

    private String header(ConsumerRecord<String, String> record, String key) {
        var h = record.headers().lastHeader(key);
        if (h == null) throw new IllegalArgumentException("Missing header: " + key);
        return new String(h.value(), StandardCharsets.UTF_8);
    }
}
