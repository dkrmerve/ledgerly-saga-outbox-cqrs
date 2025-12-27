package com.example.ledgerly.api;

import com.example.ledgerly.api.dto.AdjustStockRequest;
import com.example.ledgerly.domain.stock.Stock;
import com.example.ledgerly.infra.db.repository.StockRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;

@RestController
@RequestMapping("/api/stocks")
public class StockAdminController {

    private final StockRepository stockRepository;

    public StockAdminController(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @PostMapping("/{sku}/adjust")
    @Transactional
    public ResponseEntity<?> adjust(@PathVariable String sku, @Valid @RequestBody AdjustStockRequest req) {
        Stock s = stockRepository.findById(sku).orElseGet(() -> {
            Stock ns = new Stock();
            ns.setSku(sku);
            ns.setReserved(0);
            ns.setVersion(0);
            return ns;
        });
        s.setAvailable(req.available);
        s.setUpdatedAt(Instant.now());
        stockRepository.save(s);
        return ResponseEntity.ok().build();
    }
}
