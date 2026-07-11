CREATE TABLE payment_instructions (
    id VARCHAR(36) PRIMARY KEY,
    message_id VARCHAR(64) NOT NULL UNIQUE,
    debtor_account VARCHAR(64) NOT NULL,
    creditor_account VARCHAR(64) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    destination_country VARCHAR(2) NOT NULL,
    purpose_code VARCHAR(32) NOT NULL,
    requested_execution_date DATE NOT NULL,
    status VARCHAR(24) NOT NULL,
    status_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE payment_status_events (
    id VARCHAR(36) PRIMARY KEY,
    payment_id VARCHAR(36) NOT NULL,
    status VARCHAR(24) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_status_payment FOREIGN KEY (payment_id) REFERENCES payment_instructions(id)
);

CREATE TABLE screening_rules (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    field VARCHAR(32) NOT NULL,
    match_value VARCHAR(128) NOT NULL,
    action VARCHAR(24) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE screening_results (
    id VARCHAR(36) PRIMARY KEY,
    payment_id VARCHAR(36) NOT NULL,
    decision VARCHAR(24) NOT NULL,
    matched_rule_id VARCHAR(36),
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_screening_payment FOREIGN KEY (payment_id) REFERENCES payment_instructions(id)
);

CREATE TABLE ledger_entries (
    id VARCHAR(36) PRIMARY KEY,
    payment_id VARCHAR(36) NOT NULL,
    ledger_account VARCHAR(64) NOT NULL,
    entry_type VARCHAR(8) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_ledger_payment FOREIGN KEY (payment_id) REFERENCES payment_instructions(id)
);

CREATE TABLE audit_events (
    id VARCHAR(36) PRIMARY KEY,
    aggregate_type VARCHAR(40) NOT NULL,
    aggregate_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    details VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE batch_jobs (
    id VARCHAR(36) PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_records INT NOT NULL,
    successful_records INT NOT NULL,
    failed_records INT NOT NULL,
    error_summary VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE reconciliation_records (
    id VARCHAR(36) PRIMARY KEY,
    business_date DATE NOT NULL,
    settled_payments BIGINT NOT NULL,
    debit_total DECIMAL(19,2) NOT NULL,
    credit_total DECIMAL(19,2) NOT NULL,
    balanced BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_payment_status ON payment_instructions(status);
CREATE INDEX idx_payment_created_at ON payment_instructions(created_at);
CREATE INDEX idx_status_event_payment ON payment_status_events(payment_id);
CREATE INDEX idx_screening_result_payment ON screening_results(payment_id);
CREATE INDEX idx_ledger_payment ON ledger_entries(payment_id);
CREATE INDEX idx_audit_aggregate ON audit_events(aggregate_id);
