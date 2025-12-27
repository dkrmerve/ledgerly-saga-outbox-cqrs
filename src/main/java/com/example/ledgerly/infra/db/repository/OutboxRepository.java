package com.example.ledgerly.infra.db.repository;

import com.example.ledgerly.infra.db.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEventEntity, UUID> {

    /**
     * Postgres-native leasing: SKIP LOCKED ensures multiple instances can safely publish in parallel.
     */
    @Query(value = "SELECT * FROM outbox_events " +
            "WHERE status = 'NEW' AND available_at <= now() " +
            "AND (lock_until IS NULL OR lock_until < now()) " +
            "ORDER BY occurred_at ASC " +
            "FOR UPDATE SKIP LOCKED " +
            "LIMIT :limit", nativeQuery = true)
    List<OutboxEventEntity> leaseBatch(@Param("limit") int limit);

    @Modifying
    @Query(value = "UPDATE outbox_events SET " +
            "locked_by = :lockedBy, lock_until = :lockUntil " +
            "WHERE id = :id", nativeQuery = true)
    void markLocked(@Param("id") UUID id, @Param("lockedBy") String lockedBy, @Param("lockUntil") Instant lockUntil);
}
