CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        external_order_id VARCHAR(64) NOT NULL,
                        idempotency_key VARCHAR(64) NOT NULL,
                        status VARCHAR(32) NOT NULL,
                        total_amount NUMERIC(19,4) NOT NULL,
                        total_currency CHAR(3) NOT NULL,
                        payment_auth_id VARCHAR(64),
                        failure_reason VARCHAR(255),
                        version BIGINT NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL,
                        updated_at TIMESTAMPTZ NOT NULL,
                        UNIQUE (idempotency_key),
                        UNIQUE (external_order_id)
);

CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             sku VARCHAR(64) NOT NULL,
                             quantity INT NOT NULL,
                             unit_amount NUMERIC(19,4) NOT NULL,
                             currency CHAR(3) NOT NULL,
                             created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE stocks (
                        sku VARCHAR(64) PRIMARY KEY,
                        available INT NOT NULL,
                        reserved INT NOT NULL,
                        version BIGINT NOT NULL,
                        updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE outbox_events (
                               id UUID PRIMARY KEY,
                               topic VARCHAR(128) NOT NULL,
                               aggregate_type VARCHAR(32) NOT NULL,
                               aggregate_id VARCHAR(64) NOT NULL,
                               event_type VARCHAR(64) NOT NULL,
                               payload JSONB NOT NULL,
                               status VARCHAR(16) NOT NULL,
                               correlation_id UUID NOT NULL,
                               idempotency_key VARCHAR(64),
                               occurred_at TIMESTAMPTZ NOT NULL,
                               available_at TIMESTAMPTZ NOT NULL,
                               published_at TIMESTAMPTZ,
                               publish_attempts INT NOT NULL,
                               last_error TEXT,
                               locked_by VARCHAR(64),
                               lock_until TIMESTAMPTZ
);

CREATE TABLE inbox_events (
                              event_id UUID NOT NULL,
                              consumer VARCHAR(64) NOT NULL,
                              processed_at TIMESTAMPTZ NOT NULL,
                              PRIMARY KEY (event_id, consumer)
);

CREATE TABLE idempotency_requests (
                                      idempotency_key VARCHAR(64) PRIMARY KEY,
                                      request_hash VARCHAR(64) NOT NULL,
                                      response_code INT NOT NULL,
                                      response_body JSONB NOT NULL,
                                      created_at TIMESTAMPTZ NOT NULL
);
