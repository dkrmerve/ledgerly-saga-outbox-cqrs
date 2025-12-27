package com.example.ledgerly.infra.db.repository;

import com.example.ledgerly.domain.stock.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, String> {
    Optional<Stock> findBySku(String sku);

}
