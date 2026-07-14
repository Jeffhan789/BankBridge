# Contributing to BankBridge

Thank you for helping improve BankBridge. Keep contributions focused on the synthetic payment sandbox and preserve its safety boundaries.

## Before opening a pull request

1. Create an issue for behaviour changes or larger design work.
2. Branch from `main` and keep commits scoped.
3. Add or update tests for normal, rejected, duplicate, and failure paths as applicable.
4. Run:

   ```bash
   mvn clean verify
   docker compose config
   ```

5. Update OpenAPI-facing documentation when request or response contracts change.
6. Add a Flyway migration for schema changes; never edit an applied migration.

## Safety requirements

- Use synthetic data only.
- Do not add real credentials, account details, customer data, sanctions lists, or proprietary message formats.
- Keep secrets outside the repository and load nonlocal credentials from environment variables or a secret manager.
- Report security issues through GitHub private vulnerability reporting as described in [SECURITY.md](SECURITY.md).

## Pull requests

Explain the problem, the chosen design, verification performed, and any compatibility or migration impact. By contributing, you agree that your work is released under the repository's [MIT License](LICENSE).
