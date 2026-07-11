# Data Dictionary

## PaymentInstruction

| Field | Type | Constraint | Meaning |
|---|---|---|---|
| `id` | UUID string | primary key | Internal identifier |
| `messageId` | string | required, unique | Client idempotency key |
| `debtorAccount` | string | required | Synthetic payer account |
| `creditorAccount` | string | required, differs from debtor | Synthetic beneficiary account |
| `amount` | decimal(19,2) | greater than zero | Payment amount |
| `currency` | string(3) | letters | Educational currency code |
| `destinationCountry` | string(2) | letters | Synthetic destination |
| `purposeCode` | string | required | Generic business purpose |
| `requestedExecutionDate` | date | today or future | Requested processing date |
| `status` | enum | required | Current lifecycle state |
| `statusReason` | string | optional | Human-readable current reason |

## Supporting records

- `PaymentStatusEvent`: append-only lifecycle evidence for one payment.
- `ScreeningRule`: configurable exact-match rule using synthetic values.
- `ScreeningResult`: decision, reason, and optional matched rule ID.
- `LedgerEntry`: debit or credit record linked to one settled payment.
- `BatchJob`: file name, lifecycle, counts, and row error summary.
- `ReconciliationRecord`: date, settled count, debit total, credit total, balance flag.
- `AuditEvent`: aggregate, event type, sanitized detail, and timestamp.

All monetary values use `BigDecimal` in Java and `DECIMAL(19,2)` in SQL. Floating-point types are intentionally avoided.
