package com.example.ledgerly.infra.db.repository;

import com.example.ledgerly.infra.db.entity.InboxEventEntity;
import com.example.ledgerly.infra.db.entity.InboxEventEntityPk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxRepository extends JpaRepository<InboxEventEntity, InboxEventEntityPk> {}
