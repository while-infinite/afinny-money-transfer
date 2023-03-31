CREATE TABLE IF NOT EXISTS brokerage
(
    id                     UUID PRIMARY KEY,
    brokerage_account_name VARCHAR(255)
);

ALTER TABLE transfer_order
    ADD IF NOT EXISTS brokerage_id UUID REFERENCES brokerage (id),
    ALTER COLUMN authorization_code DROP NOT NULL,
    ALTER COLUMN currency_exchange DROP NOT NULL;

