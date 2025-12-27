package com.example.ledgerly;

import com.example.ledgerly.api.dto.AdjustStockRequest;
import com.example.ledgerly.api.dto.CreateOrderRequest;
import com.example.ledgerly.api.dto.CreateOrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class IdempotencyIT extends AbstractIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    @Test
    void createOrder_withSameIdempotencyKey_shouldReturnSameResponse() {
        seedStock("sku-2", 10);

        String idem = UUID.randomUUID().toString();

        CreateOrderRequest req = new CreateOrderRequest();
        req.externalOrderId = "EXT-" + UUID.randomUUID();
        req.currency = "EUR";
        CreateOrderRequest.Item item = new CreateOrderRequest.Item();
        item.sku = "sku-2";
        item.quantity = 1;
        item.unitAmount = 10.00;
        req.items = List.of(item);

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.add("Idempotency-Key", idem);
        h.add("X-Correlation-Id", UUID.randomUUID().toString());

        ResponseEntity<CreateOrderResponse> r1 = rest.exchange("/api/orders", HttpMethod.POST, new HttpEntity<>(req, h), CreateOrderResponse.class);
        ResponseEntity<CreateOrderResponse> r2 = rest.exchange("/api/orders", HttpMethod.POST, new HttpEntity<>(req, h), CreateOrderResponse.class);

        assertThat(r1.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(r2.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(r1.getBody()).isNotNull();
        assertThat(r2.getBody()).isNotNull();

        assertThat(r2.getBody().orderId).isEqualTo(r1.getBody().orderId);
        assertThat(r2.getBody().status).isEqualTo(r1.getBody().status);
    }

    private void seedStock(String sku, int available) {
        AdjustStockRequest s = new AdjustStockRequest();
        s.available = available;

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> res = rest.exchange(
                "/api/stocks/" + sku + "/adjust",
                HttpMethod.POST,
                new HttpEntity<>(s, h),
                Void.class
        );
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
    }
}
