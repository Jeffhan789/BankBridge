# Contributing to BankBridge

Thank you for your interest in contributing to BankBridge (汇桥)! This document provides guidelines and instructions for contributing.

## 🚀 Quick Start

1. Fork the repository
2. Clone your fork: `git clone https://github.com/<your-username>/BankBridge.git`
3. Build the project: `mvn clean verify`

## 📋 Development Requirements

- **Java 21** (OpenJDK or Eclipse Temurin)
- **Maven 3.9+**
- **Docker & Docker Compose** (for integration tests with Testcontainers)

## 🔧 Development Setup

```bash
# Run all tests (including integration tests with Testcontainers)
mvn clean verify

# Run only unit tests
mvn test

# Run the application locally with Docker Compose
docker compose up --build
```

## 🧪 Testing Guidelines

- All new features must include unit tests
- Integration tests use Testcontainers (MySQL, RabbitMQ)
- JWT tests require `JWT_SECRET` environment variable (set automatically in CI)
- Ensure `mvn clean verify` passes before submitting a PR

## 📝 Code Style

- Follow standard Java conventions (Google Java Style)
- Use meaningful variable and method names
- Add JavaDoc for public APIs
- Keep methods focused and single-responsibility

## 🌿 Branch Strategy

- `main` — stable, production-ready code
- Feature branches: `feature/<short-description>`
- Bugfix branches: `fix/<short-description>`

## 📤 Submitting Changes

1. Create a new branch from `main`
2. Make your changes with clear, focused commits
3. Add/update tests as needed
4. Update documentation if you change behavior
5. Push to your fork and open a Pull Request

## 🏷️ Commit Message Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>
```

Types:
- `feat:` — New feature
- `fix:` — Bug fix
- `docs:` — Documentation only
- `style:` — Code style (formatting, no logic change)
- `refactor:` — Code refactoring
- `test:` — Adding or updating tests
- `chore:` — Maintenance, dependencies, CI

Examples:
```
feat(auth): add JWT refresh token endpoint
fix(payment): resolve idempotency key collision on retry
docs(readme): update API endpoint table
```

## 🔒 Security

- Never commit real credentials or secrets
- Use environment variables for sensitive configuration
- Report security issues privately to the maintainers

## 📜 License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).

## 💬 Questions?

Feel free to open an issue for questions or discussion. For bug reports, please use the bug report template.

---

感谢您对 BankBridge（汇桥）的贡献！如有疑问，欢迎提交 Issue 讨论。
