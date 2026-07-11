# Test Scenarios

## Automated acceptance matrix

| Scenario | Input | Expected result |
|---|---|---|
| Normal settlement | Valid synthetic payment | `SETTLED`; six status events; two ledger entries |
| Reject rule | Creditor `TEST-BLOCKED-001` | `REJECTED`; matched rule; no ledger entries |
| Manual review | Destination `ZZ` | `MANUAL_REVIEW`; no ledger entries |
| Duplicate message | Submit same `messageId` twice | First `201`; second `409` |
| Invalid amount | Amount zero or negative | `400`; no payment created |
| Invalid execution date | Past date | `400`; no payment created |
| Mixed batch | One valid and one invalid row | `COMPLETED_WITH_ERRORS`; row counts preserved |
| Reconciliation | One or more settled payments | Debit total equals credit total |
| Unknown resource | Unknown payment or batch ID | `404` problem response |

## Commands

```bash
mvn test
mvn verify
```

The main integration suite runs with H2 in MySQL compatibility mode for speed. A Testcontainers smoke test validates Flyway and JPA against MySQL when Docker is available; it is skipped when Docker is unavailable.

## Release gate

- all Maven tests pass;
- no secrets or organization-specific terms are present;
- sample payloads use synthetic values;
- README commands match the implemented endpoints;
- the packaged JAR starts against MySQL;
- CI passes on the release commit.
