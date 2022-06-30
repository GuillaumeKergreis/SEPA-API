CREATE DATABASE sepa_api;

CREATE TABLE sepa_api.sepa_credit_transfer_transaction
(
    instruction_id VARCHAR,
    end_to_end_id VARCHAR,
    sepa_tx_id VARCHAR,
    amount DECIMAL,
    debtor_name VARCHAR,
    debtor_account VARCHAR,
    debtor_agent VARCHAR,
    creditor_name VARCHAR,
    creditor_account VARCHAR,
    creditor_agent VARCHAR,
    purpose_code VARCHAR,
    description VARCHAR
);