package com.example.ledgerly.infra.db.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface StockAtomicRepository extends Repository<com.example.ledgerly.domain.stock.Stock, String> {

    /**
     * Atomic invariant update:
     * - prevents oversell without relying solely on read-check-write
     * - combined with optimistic version check
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE stocks " +
            "SET available = available - :qty, reserved = reserved + :qty, version = version + 1, updated_at = now() " +
            "WHERE sku = :sku AND available >= :qty AND version = :expectedVersion", nativeQuery = true)
    int reserveAtomic(@Param("sku") String sku, @Param("qty") int qty, @Param("expectedVersion") long expectedVersion);

    @Modifying
    @Transactional
    @Query(value = "UPDATE stocks " +
            "SET available = available + :qty, reserved = reserved - :qty, version = version + 1, updated_at = now() " +
            "WHERE sku = :sku AND reserved >= :qty AND version = :expectedVersion", nativeQuery = true)
    int releaseAtomic(@Param("sku") String sku, @Param("qty") int qty, @Param("expectedVersion") long expectedVersion);
}
