package com.example.ledgerly;

import com.example.ledgerly.api.dto.AdjustStockRequest;
import com.example.ledgerly.api.dto.CreateOrderRequest;
import com.example.ledgerly.api.dto.CreateOrderResponse;
import com.example.ledgerly.infra.payment.MasterpassClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentAuthorizeFailIT extends AbstractIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    @MockBean
    MasterpassClient masterpassClient;

    @Test
    void paymentAuthorizeFails_shouldCancelOrder_noCompletion() {
        seedStock("sku-payfail", 10);

        Mockito.when(masterpassClient.authorize(Mockito.any()))
                .thenThrow(new RuntimeException("Masterpass authorize failed (test)"));

        String idem = UUID.randomUUID().toString();

        CreateOrderRequest req = new CreateOrderRequest();
        req.externalOrderId = "EXT-" + UUID.randomUUID();
        req.currency = "EUR";

        CreateOrderRequest.Item item = new CreateOrderRequest.Item();
        item.sku = "sku-payfail";
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

        long orderId = created.getBody().orderId;

        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(300))
                .untilAsserted(() -> {
                    ResponseEntity<JsonNode> write = rest.getForEntity("/api/query/orders/" + orderId, JsonNode.class);
                    assertThat(write.getStatusCode().is2xxSuccessful()).isTrue();

                    String status = write.getBody().get("status").asText();

                    assertThat(status).isIn("CANCELLED", "PAYMENT_FAILED");

                    if (write.getBody().hasNonNull("paymentAuthId")) {
                        assertThat(write.getBody().get("paymentAuthId").asText()).isNotBlank();
                    }
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
