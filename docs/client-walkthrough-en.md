# Three-Minute Client Walkthrough

## Opening

BankBridge is an independent educational sandbox that demonstrates a reliable payment-integration workflow using synthetic data. It is not connected to a real bank or payment network. Current version: v0.3.0.

## Business flow

A client authenticates with username and password and receives a JWT access token. It then submits a payment instruction with a unique message ID. The gateway validates required business fields and checks the message ID for duplicates. It then evaluates configurable synthetic screening rules. A payment may be rejected, sent to manual review, or accepted.

An accepted payment is handed to asynchronous settlement: the service records a settlement event in the same database transaction, a publisher relays it to RabbitMQ, and a settlement worker posts one debit and one equal credit entry, verifying that the posting is balanced before marking the payment as settled. Every transition is stored in the status history and audit log with the operator's identity.

## Reliability evidence

The transactional outbox guarantees that a database commit and the message to the settlement queue can never diverge. The consumer is idempotent, so a redelivered message can never post twice. Transient failures are retried with exponential backoff; messages that exhaust retries land in a dead-letter queue and can be replayed by an administrator after the root cause is fixed.

## Security evidence

Stateless JWT authentication protects every business endpoint. Four roles (OPERATOR, COMPLIANCE_ANALYST, AUDITOR, ADMIN) form an RBAC matrix enforced at method level: operators submit payments, analysts review screening, auditors read reconciliation and compliance reports, and only administrators maintain rules or replay dead letters. Non-administrator roles see account identifiers in masked form (for example `GB12****5678`), while the stored data remains complete for reconciliation.

## Integration evidence

The OpenAPI page documents the request, response, and error contracts. A duplicate message returns HTTP 409, so a client can safely retry without creating a second payment. CSV batches isolate bad rows instead of discarding valid records. Daily reconciliation proves that total debits equal total credits.

## Security boundary

All accounts, rules, and transactions are synthetic. The project deliberately excludes real messages, customer data, credentials, and official regulatory templates. Secrets for non-local environments are supplied through environment variables only.

## Roadmap

The current version delivers the reliable asynchronous core with authentication and role-based access control. Planned iterations add an operations dashboard with metrics and Grafana dashboards (v0.4), performance baselines and resilience testing (v0.5), and a frozen v1.0 API with architecture decision records and a recorded demo.
