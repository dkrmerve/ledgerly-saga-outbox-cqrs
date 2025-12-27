package com.example.ledgerly.application.idempotency;

import com.example.ledgerly.infra.db.entity.IdempotencyEntity;
import com.example.ledgerly.infra.db.repository.IdempotencyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class IdempotencyService {

    private final IdempotencyRepository repo;
    private final ObjectMapper om;

    public IdempotencyService(IdempotencyRepository repo, ObjectMapper om) {
        this.repo = repo;
        this.om = om;
    }

    /**
     * Executes an action exactly-once per (idempotencyKey + requestHash).
     *
     * Behavior:
     * - If key exists and requestHash matches: returns stored response (same code/body)
     * - If key exists but requestHash differs: returns 409 (idempotency violation)
     * - If key doesn't exist: executes action, stores response, returns it
     */
    public ResponseEntity<?> execute(String rawIdempotencyKey, Object request, Supplier<?> action) {
        String key = hashUtf8(rawIdempotencyKey);         // stable DB key, avoids huge header values
        String requestHash = sha256Json(request);

        Optional<IdempotencyEntity> existing = repo.findById(key);

        if (existing.isPresent()) {
            IdempotencyEntity saved = existing.get();

            if (!requestHash.equals(saved.getRequestHash())) {
                return ResponseEntity.status(409).body(errorJson("IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_REQUEST"));
            }

            Object body = parseJsonOrString(saved.getResponseBody());
            return ResponseEntity.status(saved.getResponseCode()).body(body);
        }

        Object response = action.get();
        return saveAndReturn(key, requestHash, response);
    }

    @Transactional
    ResponseEntity<?> saveAndReturn(String key, String requestHash, Object response) {
        try {
            String responseJson = om.writeValueAsString(response);

            IdempotencyEntity e = new IdempotencyEntity();
            e.setIdempotencyKey(key);
            e.setRequestHash(requestHash);
            e.setResponseCode(200); // if you later support non-200, pass it as param
            e.setResponseBody(responseJson);
            e.setCreatedAt(Instant.now());

            repo.save(e);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to persist idempotency result", ex);
        }
    }

    private String sha256Json(Object obj) {
        try {
            byte[] bytes = om.writeValueAsBytes(obj);
            return sha256Hex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash request JSON", e);
        }
    }

    /**
     * Java 11 compatible SHA-256 hex.
     */
    private static String sha256Hex(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            return toHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Hash for raw string key. (Also SHA-256 hex)
     */
    private static String hashUtf8(String raw) {
        return sha256Hex(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private Object parseJsonOrString(String json) {
        try {
            JsonNode node = om.readTree(json);
            return node;
        } catch (Exception e) {
            return json;
        }
    }

    private JsonNode errorJson(String code) {
        return om.createObjectNode()
                .put("error", code);
    }
}
