# Batch Processing Flow

## Input contract

The endpoint accepts one UTF-8 CSV file up to 2 MB. The header must exactly match the interface specification. Every nonblank row is treated as one payment instruction.

## Processing

```mermaid
flowchart TD
    A["Upload CSV"] --> B{"Header valid?"}
    B -- No --> C["Mark batch FAILED"]
    B -- Yes --> D["Read next row"]
    D --> E{"Row valid?"}
    E -- Yes --> F["Process through PaymentService"]
    E -- No --> G["Record line error"]
    F --> H{"More rows?"}
    G --> H
    H -- Yes --> D
    H -- No --> I{"Any errors?"}
    I -- No --> J["COMPLETED"]
    I -- Yes --> K["COMPLETED_WITH_ERRORS"]
```

Successful rows remain committed when another row fails. The response reports total, successful, and failed record counts plus a line-numbered error summary.

## Failure policy

- Invalid file or header: fail the complete batch.
- Invalid row: isolate the row and continue.
- Duplicate `messageId`: record the row as failed.
- Screening rejection: the row is technically processed successfully because the payment reached a valid business decision.
- Unexpected input/output error: mark the batch failed and return `400`.

`v0.2` will move large batches to background workers and add downloadable result files.
