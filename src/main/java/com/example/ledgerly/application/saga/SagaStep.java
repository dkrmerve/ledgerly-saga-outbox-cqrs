package com.example.ledgerly.application.saga;

/**
 * A SagaStep represents one local action in a saga orchestration.
 * It can be executed and (optionally) compensated.
 *
 * T is typically an aggregate root (e.g., Order).
 */
public interface SagaStep<T> {

    /**
     * Executes the step. Must be side-effect safe under retries (idempotent effect).
     * correlationId is used for tracing/logging across distributed calls.
     */
    SagaStepResult execute(T aggregate, String correlationId);

    /**
     * Compensates the step if later steps fail.
     * Compensation must be best-effort and idempotent as well.
     */
    void compensate(T aggregate, String correlationId);
}
