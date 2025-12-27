package com.example.ledgerly.infra.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderViewRepository extends MongoRepository<OrderView, Long> {}
