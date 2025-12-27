package com.example.ledgerly.observability;

import java.util.Optional;
import java.util.UUID;

public final class Correlation {
    private Correlation() {}

    public static UUID parseOrNew(String value) {
        try {
            return value == null || value.isBlank() ? UUID.randomUUID() : UUID.fromString(value);
        } catch (Exception e) {
            return UUID.randomUUID();
        }
    }

    public static Optional<String> nonBlank(String v) {
        return (v == null || v.isBlank()) ? Optional.empty() : Optional.of(v);
    }
}
