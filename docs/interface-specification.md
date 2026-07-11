# Interface Specification

## Conventions

- Base URL: `http://localhost:8080`
- Media type: `application/json`, except CSV upload
- Dates: ISO `YYYY-MM-DD`
- Currency: three uppercase letters
- Country: two uppercase letters
- Errors: RFC 9457-style `application/problem+json`

## Submit payment

`POST /api/payments`

Required fields:

```json
{
  "messageId": "PAY-DEMO-001",
  "debtorAccount": "TEST-DEBTOR-001",
  "creditorAccount": "TEST-CREDITOR-001",
  "amount": 12500.00,
  "currency": "CNY",
  "destinationCountry": "SG",
  "purposeCode": "GOODS",
  "requestedExecutionDate": "2099-01-01"
}
```

Responses:

- `201 Created`: payment created; response includes state history, screening decision, and any ledger entries.
- `400 Bad Request`: schema or business validation failed.
- `409 Conflict`: `messageId` already exists.

## Read payment

`GET /api/payments/{id}`

- `200 OK`: complete payment view.
- `404 Not Found`: unknown internal ID.

## Upload batch

`POST /api/payment-batches` using `multipart/form-data` field `file`.

The CSV header is exact and versioned:

```text
messageId,debtorAccount,creditorAccount,amount,currency,destinationCountry,purposeCode,requestedExecutionDate
```

Quoted commas are not supported in `v0.1.0`; none of the defined fields require free-text commas.

## Reports

- `GET /api/reconciliation/daily?date=YYYY-MM-DD`
- `GET /api/compliance-reports/daily?date=YYYY-MM-DD`

When `date` is omitted, the server uses its current date. Report fields are educational summaries, not regulatory submissions.

## Screening rules

`POST /api/screening-rules`

```json
{
  "name": "Synthetic purpose review",
  "field": "PURPOSE_CODE",
  "matchValue": "REVIEW_TEST",
  "action": "MANUAL_REVIEW",
  "active": true
}
```

Supported fields: `DEBTOR_ACCOUNT`, `CREDITOR_ACCOUNT`, `DESTINATION_COUNTRY`, `PURPOSE_CODE`.
