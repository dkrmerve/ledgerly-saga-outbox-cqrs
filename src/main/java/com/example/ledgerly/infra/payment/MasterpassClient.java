package com.example.ledgerly.infra.payment;

import com.example.ledgerly.infra.payment.dto.*;

import java.util.Objects;

public interface MasterpassClient {

    // Single source of truth: DTO-based API
    MasterpassAuthorizeResponse authorize(MasterpassAuthorizeRequest request);
    MasterpassCaptureResponse capture(MasterpassCaptureRequest request);
    MasterpassVoidResponse voidAuth(MasterpassVoidRequest request);

    // Convenience adapter methods: keeps callers simple, avoids duplicating contracts
    default MasterpassAuthorizeResponse authorize(long orderId, String amount, String currency, String idempotencyKey) {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");

        MasterpassAuthorizeRequest req = new MasterpassAuthorizeRequest();
        req.setOrderId(orderId);
        req.setAmount(amount);
        req.setCurrency(currency);
        req.setIdempotencyKey(idempotencyKey);

        return authorize(req);
    }

    default MasterpassCaptureResponse capture(String authId, String idempotencyKey) {
        Objects.requireNonNull(authId, "authId");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");

        MasterpassCaptureRequest req = new MasterpassCaptureRequest();
        req.setAuthId(authId);
        req.setIdempotencyKey(idempotencyKey);

        return capture(req);
    }

    default MasterpassVoidResponse voidAuth(String authId, String idempotencyKey) {
        Objects.requireNonNull(authId, "authId");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");

        MasterpassVoidRequest req = new MasterpassVoidRequest();
        req.setAuthId(authId);
        req.setIdempotencyKey(idempotencyKey);

        return voidAuth(req);
    }
}
