ALTER TABLE transfer_order
    ADD if not exists operation_type VARCHAR NOT NULL DEFAULT 'INCOME';