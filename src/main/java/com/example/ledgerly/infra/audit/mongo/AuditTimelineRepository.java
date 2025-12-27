package com.example.ledgerly.infra.audit.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditTimelineRepository extends MongoRepository<AuditTimelineDocument, String> {}
