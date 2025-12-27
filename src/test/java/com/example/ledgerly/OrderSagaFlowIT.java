package com.example.ledgerly;

import com.example.ledgerly.api.dto.CreateOrderRequest;
import com.example.ledgerly.api.dto.CreateOrderResponse;
import com.example.ledgerly.api.dto.AdjustStockRequest;
import com.fasterxml.jackson.databind.JsonNode;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderSagaFlowIT extends AbstractIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    @Test
    void happyPath_shouldCompleteOrder_andPopulateReadModel() {
        seedStock("sku-1", 10);

        String idem = UUID.randomUUID().toString();

        CreateOrderRequest req = new CreateOrderRequest();
        req.externalOrderId = "EXT-" + UUID.randomUUID();
        req.currency = "EUR";

        CreateOrderRequest.Item item = new CreateOrderRequest.Item();
        item.sku = "sku-1";
        item.quantity = 1;
        item.unitAmount = 12.34;
        req.items = List.of(item);

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.add("Idempotency-Key", idem);
        h.add("X-Correlation-Id", UUID.randomUUID().toString());

        ResponseEntity<CreateOrderResponse> created = rest.exchange(
                "/api/orders",
                HttpMethod.POST,
                new HttpEntity<>(req, h),
                CreateOrderResponse.class
        );

        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(created.getBody()).isNotNull();
        long orderId = created.getBody().orderId;

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    ResponseEntity<JsonNode> write = rest.getForEntity("/api/query/orders/" + orderId, JsonNode.class);
                    assertThat(write.getStatusCode().is2xxSuccessful()).isTrue();
                    assertThat(write.getBody().get("status").asText()).isEqualTo("COMPLETED");
                });

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    ResponseEntity<JsonNode> view = rest.getForEntity("/api/query/order-views/" + orderId, JsonNode.class);
                    assertThat(view.getStatusCode().is2xxSuccessful()).isTrue();
                    assertThat(view.getBody().get("status").asText()).isEqualTo("COMPLETED");
                });
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
