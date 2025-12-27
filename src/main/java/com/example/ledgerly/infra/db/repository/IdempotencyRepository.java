package com.example.ledgerly.infra.db.repository;

import com.example.ledgerly.infra.db.entity.IdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRepository extends JpaRepository<IdempotencyEntity, String> {}
