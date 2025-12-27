package com.example.ledgerly.infra.payment;

import com.example.ledgerly.infra.payment.dto.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile({"test", "stub"}) // testlerde otomatik seçilsin
public class MasterpassStubClient implements MasterpassClient {

    @Override
    public MasterpassAuthorizeResponse authorize(MasterpassAuthorizeRequest request) {
        // deterministic rules for demo:
        // - if amount == "0" or empty -> decline
        if (request.getAmount() == null || request.getAmount().trim().isEmpty() || "0".equals(request.getAmount().trim())) {
            return new MasterpassAuthorizeResponse(null, "DECLINED", "invalid amount");
        }
        return new MasterpassAuthorizeResponse("AUTH-" + UUID.randomUUID(), "AUTHORIZED", "ok");
    }

    @Override
    public MasterpassCaptureResponse capture(MasterpassCaptureRequest request) {
        if (request.getAuthId() == null || request.getAuthId().isBlank()) {
            return new MasterpassCaptureResponse("FAILED", "missing authId");
        }
        return new MasterpassCaptureResponse("CAPTURED", "ok");
    }

    @Override
    public MasterpassVoidResponse voidAuth(MasterpassVoidRequest request) {
        if (request.getAuthId() == null || request.getAuthId().isBlank()) {
            return new MasterpassVoidResponse("FAILED", "missing authId");
        }
        return new MasterpassVoidResponse("VOIDED", "ok");
    }
}
