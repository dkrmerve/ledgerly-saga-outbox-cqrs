package com.example.ledgerly;

import com.example.ledgerly.api.dto.AdjustStockRequest;
import com.example.ledgerly.api.dto.CreateOrderRequest;
import com.example.ledgerly.api.dto.CreateOrderResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class OptimisticLockRaceIT extends AbstractIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    @Test
    void twoOrdersCompeteForSingleStock_oneCompletes_otherCancels_noOversell() throws Exception {
        seedStock("sku-race", 1);

        ExecutorService pool = Executors.newFixedThreadPool(2);

        Callable<Long> createOrder = () -> {
            String idem = UUID.randomUUID().toString();
            CreateOrderRequest req = new CreateOrderRequest();
            req.externalOrderId = "EXT-" + UUID.randomUUID();
            req.currency = "EUR";

            CreateOrderRequest.Item item = new CreateOrderRequest.Item();
            item.sku = "sku-race";
            item.quantity = 1;
            item.unitAmount = 10.00;
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
            return created.getBody().orderId;
        };

        Future<Long> f1 = pool.submit(createOrder);
        Future<Long> f2 = pool.submit(createOrder);

        long o1 = f1.get(10, TimeUnit.SECONDS);
        long o2 = f2.get(10, TimeUnit.SECONDS);

        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(300))
                .untilAsserted(() -> {
                    String s1 = getStatus(o1);
                    String s2 = getStatus(o2);
                    boolean oneCompleted = ("COMPLETED".equals(s1) && "CANCELLED".equals(s2))
                            || ("CANCELLED".equals(s1) && "COMPLETED".equals(s2));
                    assertThat(oneCompleted).isTrue();
                });

        pool.shutdownNow();
    }

    private String getStatus(long orderId) {
        ResponseEntity<JsonNode> write = rest.getForEntity("/api/query/orders/" + orderId, JsonNode.class);
        assertThat(write.getStatusCode().is2xxSuccessful()).isTrue();
        return write.getBody().get("status").asText();
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
