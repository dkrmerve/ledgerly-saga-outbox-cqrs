CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_outbox_status_available ON outbox_events(status, available_at);
CREATE INDEX idx_outbox_lock_until ON outbox_events(lock_until);
CREATE INDEX idx_outbox_aggregate ON outbox_events(aggregate_type, aggregate_id);
CREATE INDEX idx_inbox_processed_at ON inbox_events(processed_at);
