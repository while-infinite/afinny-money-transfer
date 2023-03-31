CREATE TABLE if not exists template_for_payment
(
    id                          SERIAL       PRIMARY KEY,
    name                        VARCHAR(255) NOT NULL,
    payee_name                  VARCHAR(255) NOT NULL,
    payee_account_number        VARCHAR(30) NOT NULL,
    template_purpose_of_payment VARCHAR(255) NOT NULL,
    bic                         VARCHAR(9)   NOT NULL,
    inn                         VARCHAR(12)  NOT NULL
);

CREATE TABLE if not exists payee
(
    id                   UUID         PRIMARY KEY,
    type                 VARCHAR(255) NOT NULL,
    name                 VARCHAR(255),
    inn                  VARCHAR(12),
    bic                  VARCHAR(9)   NOT NULL,
    payee_account_number VARCHAR(255) NOT NULL,
    payee_card_number    VARCHAR(255)
);

CREATE TABLE if not exists additional_parameters
(
    id          SERIAL       PRIMARY KEY,
    key         VARCHAR(255) NOT NULL,
    value       VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    payee_id    UUID         NOT NULL REFERENCES payee (id)
);

CREATE TABLE if not exists transfer_type
(
    id                 SERIAL         PRIMARY KEY,
    type_name          VARCHAR(255)   NOT NULL,
    currency_code      VARCHAR(3)     NOT NULL,
    min_commission     NUMERIC(19, 4) NOT NULL,
    max_commission     NUMERIC(19, 4) NOT NULL,
    percent_commission NUMERIC(19, 4),
    fix_commission     NUMERIC(19, 4),
    min_sum            NUMERIC(19, 4) NOT NULL,
    max_sum            NUMERIC(19, 4) NOT NULL,
    CONSTRAINT max_commission_check   CHECK ( max_commission >= min_commission ),
    CONSTRAINT max_sum_check          CHECK ( max_sum >= min_sum ),
    CONSTRAINT commission_check       CHECK ( (percent_commission IS NOT NULL AND fix_commission IS NULL)
        OR (fix_commission IS NOT NULL AND percent_commission IS NULL) )
);

CREATE TABLE if not exists transfer_order
(
    id                   UUID                        PRIMARY KEY,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    transfer_type_id     INTEGER                     NOT NULL REFERENCES transfer_type (id),
    purpose              TEXT,
    remitter_card_number VARCHAR(16)                 NOT NULL,
    payee_id             UUID                        NOT NULL REFERENCES payee (id),
    sum                  NUMERIC(19, 4)              NOT NULL,
    sum_commission       NUMERIC(19, 4)              NOT NULL,
    completed_at         TIMESTAMP WITHOUT TIME ZONE,
    status               VARCHAR(255)                NOT NULL,
    authorization_code   VARCHAR(255)                NOT NULL,
    currency_exchange    NUMERIC(19, 4)              NOT NULL,
    is_favorite          BOOLEAN                     NOT NULL,
    start_date           TIMESTAMP WITHOUT TIME ZONE,
    periodicity          VARCHAR(255)
);