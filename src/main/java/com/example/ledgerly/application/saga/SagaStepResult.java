// src/main/java/com/example/ledgerly/application/saga/SagaStepResult.java
package com.example.ledgerly.application.saga;

import java.util.Objects;

/**
 * Result of a Saga step execution.
 * We keep it explicit: SUCCESS or FAILURE with an errorCode for audit/observability.
 */
public final class SagaStepResult {

    public enum Status {
        SUCCESS,
        FAILURE
    }

    private final Status status;
    private final String errorCode;

    private SagaStepResult(Status status, String errorCode) {
        this.status = Objects.requireNonNull(status, "status");
        this.errorCode = errorCode;
    }

    public static SagaStepResult success() {
        return new SagaStepResult(Status.SUCCESS, null);
    }

    public static SagaStepResult failure(String errorCode) {
        if (errorCode == null || errorCode.trim().isEmpty()) {
            throw new IllegalArgumentException("errorCode must be provided for failure results");
        }
        return new SagaStepResult(Status.FAILURE, errorCode);
    }

    public Status getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isFailure() {
        return status == Status.FAILURE;
    }
}
