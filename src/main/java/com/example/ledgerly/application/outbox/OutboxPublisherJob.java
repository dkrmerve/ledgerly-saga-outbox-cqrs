package com.example.ledgerly.application.outbox;

import com.example.ledgerly.infra.db.entity.OutboxEventEntity;
import com.example.ledgerly.infra.db.repository.OutboxRepository;
import com.example.ledgerly.infra.kafka.OutboxKafkaPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.Instant;
import java.util.List;

// AWS note (design-only):
// This publisher can run as:
// - a dedicated ECS/Fargate worker service,
// - a Kubernetes (EKS) Deployment,
// and scales horizontally because Postgres leasing uses SKIP LOCKED.

@EnableScheduling
@Component
public class OutboxPublisherJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherJob.class);

    private final OutboxRepository outboxRepository;
    private final OutboxKafkaPublisher publisher;

    private final int batchSize;
    private final int leaseSeconds;
    private final String nodeId;

    public OutboxPublisherJob(OutboxRepository outboxRepository,
                              OutboxKafkaPublisher publisher,
                              @Value("${ledgerly.outbox.batchSize}") int batchSize,
                              @Value("${ledgerly.outbox.leaseSeconds}") int leaseSeconds) throws Exception {
        this.outboxRepository = outboxRepository;
        this.publisher = publisher;
        this.batchSize = batchSize;
        this.leaseSeconds = leaseSeconds;
        this.nodeId = InetAddress.getLocalHost().getHostName();
    }

    @Scheduled(fixedDelayString = "${ledgerly.outbox.publishFixedDelayMs}")
    @Transactional
    public void publishLoop() {
        List<OutboxEventEntity> batch = outboxRepository.leaseBatch(batchSize);
        if (batch.isEmpty()) return;

        Instant lockUntil = Instant.now().plusSeconds(leaseSeconds);
        for (OutboxEventEntity e : batch) {
            outboxRepository.markLocked(e.getId(), nodeId, lockUntil);
            try {
                publisher.publish(e);
                e.setStatus("PUBLISHED");
                e.setPublishedAt(Instant.now());
                e.setPublishAttempts(e.getPublishAttempts() + 1);
                e.setLastError(null);
            } catch (Exception ex) {
                e.setPublishAttempts(e.getPublishAttempts() + 1);
                e.setLastError(ex.getMessage());
                // keep NEW so it retries; lock expires.
                e.setStatus("NEW");
                log.error("Outbox publish failed: {}", ex.getMessage());
            }
            outboxRepository.save(e);
        }
    }
}
