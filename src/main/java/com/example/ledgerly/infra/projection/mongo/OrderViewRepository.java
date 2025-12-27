package com.example.ledgerly.infra.projection.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderViewRepository extends MongoRepository<OrderViewDocument, String> {}
