package com.example.ledgerly.application.inbox;

import com.example.ledgerly.infra.db.entity.InboxEventEntity;
import com.example.ledgerly.infra.db.repository.InboxRepository;
import com.example.ledgerly.infra.redis.RedisDedupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class InboxService {

    private final InboxRepository inboxRepository;
    private final RedisDedupService redisDedupService;

    public InboxService(InboxRepository inboxRepository, RedisDedupService redisDedupService) {
        this.inboxRepository = inboxRepository;
        this.redisDedupService = redisDedupService;
    }

    /**
     * Exactly-once effects:
     * 1) Redis: fast path "have we likely seen it?"
     * 2) DB Inbox: authoritative unique constraint (eventId, consumer) within the same TX as side effects.
     */
    @Transactional
    public boolean claim(String consumer, UUID eventId) {
        boolean likelyFirst = redisDedupService.firstTime(consumer, eventId.toString());
        if (!likelyFirst) {
            // Might be duplicate; DB check is authoritative below.
        }

        InboxEventEntity e = new InboxEventEntity();
        e.setEventId(eventId);
        e.setConsumer(consumer);
        e.setProcessedAt(Instant.now());

        try {
            inboxRepository.save(e); // PK violation -> duplicate
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
