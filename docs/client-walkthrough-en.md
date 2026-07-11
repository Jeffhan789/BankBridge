# Three-Minute Client Walkthrough

## Opening

BankBridge is an independent educational sandbox that demonstrates a reliable payment-integration workflow using synthetic data. It is not connected to a real bank or payment network.

## Business flow

A client submits a payment instruction with a unique message ID. The gateway validates required business fields and checks the message ID for duplicates. It then evaluates configurable synthetic screening rules. A payment may be rejected, sent to manual review, or accepted.

An accepted payment moves into processing. The service creates one debit and one equal credit entry and verifies that the posting is balanced before marking the payment as settled. Every transition is stored in the status history and audit log.

## Integration evidence

The OpenAPI page documents the request, response, and error contracts. A duplicate message returns HTTP 409, so a client can safely retry without creating a second payment. CSV batches isolate bad rows instead of discarding valid records. Daily reconciliation proves that total debits equal total credits.

## Security boundary

All accounts, rules, and transactions are synthetic. The project deliberately excludes real messages, customer data, credentials, and official regulatory templates. In a production environment I would add authentication, encryption, masked identifiers, a secrets manager, and formal access-controlled audit retention.

## Roadmap

The current version is synchronous for deterministic testing. The next version moves accepted payments to RabbitMQ and adds retry, timeout, and dead-letter behavior without changing the public payment API.
