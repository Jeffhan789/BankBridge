# Security Boundaries

## Data policy

- Use synthetic account names beginning with `TEST-`.
- Never upload real account numbers, customer records, transaction histories, screening lists, credentials, or internal documents.
- The included rules are fictional and exist only to make test paths deterministic.
- Repository examples do not name real organizations or imply an integration relationship.

## Secret handling

- Local Docker passwords are development-only and isolated to `docker-compose.yml`.
- Nonlocal credentials must be supplied through environment variables.
- `.env` is ignored; `.env.example` contains placeholders only.
- GitHub Actions receives no database or cloud secret in `v0.1.0`.

## Logging and audit

- Audit details record state and reason, not credentials.
- API responses expose synthetic account identifiers because this is a local educational system.
- A production design would mask account identifiers, encrypt sensitive columns, use authenticated actors, and separate operational logs from audit evidence.

## AI-assisted development boundary

AI tools may assist with public, synthetic code and documentation. They must not receive confidential source code, customer data, real payment messages, security keys, or internal system designs. Generated changes require tests and human review before release.

## Non-goals

BankBridge is not a payment processor, compliance product, sanctions engine, accounting platform, or certified reporting solution. Its rules and reports must not be used for financial decisions.
