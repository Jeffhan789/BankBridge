# BankBridge

> English first. 中文版见后文。

[English](#english-overview) | [中文](#中文项目介绍)

[![CI](https://github.com/Jeffhan789/BankBridge/actions/workflows/ci.yml/badge.svg)](https://github.com/Jeffhan789/BankBridge/actions/workflows/ci.yml)
[![Java 21](https://img.shields.io/badge/Java-21-007396)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**A cross-border payment, reconciliation, and compliance-processing sandbox for backend engineering practice.**

BankBridge models how a payment instruction moves from intake to validation, synthetic compliance screening, double-entry posting, settlement, reconciliation, and reporting. It focuses on the engineering work behind reliable financial integrations: state transitions, idempotency, batch isolation, auditable decisions, database migrations, API contracts, and automated tests.

> **Independent educational project.** This sandbox uses synthetic data only. It is not affiliated with any bank, payment network, regulator, or financial technology company. It does not connect to real financial infrastructure or implement an official regulatory message format.

## English Overview

BankBridge is a portfolio-ready backend engineering project that models a complete synthetic payment-processing lifecycle. It is designed to demonstrate reliable service design rather than a simple CRUD application: requests move through validation, synthetic compliance screening, double-entry posting, settlement, reconciliation, reporting, and auditable state history.

The current `v0.1.0` implementation is synchronous and deterministic so that each state transition can be reproduced, inspected, and tested. Planned asynchronous messaging, observability, and resilience work is listed separately in the roadmap and is not presented as already implemented.

## What the project demonstrates

- Java 21 and Spring Boot API design
- MySQL persistence with Flyway migrations
- duplicate-message protection through a unique business key
- configurable synthetic reject and manual-review rules
- balanced debit and credit postings for settled payments
- isolated CSV batch failures
- daily reconciliation and generic compliance summaries
- immutable status history and searchable audit events
- OpenAPI documentation, Docker packaging, and CI

## Payment lifecycle

```mermaid
stateDiagram-v2
    [*] --> RECEIVED
    RECEIVED --> VALIDATED
    VALIDATED --> SCREENING
    SCREENING --> REJECTED: reject rule
    SCREENING --> MANUAL_REVIEW: review rule
    SCREENING --> ACCEPTED: no rule matched
    ACCEPTED --> PROCESSING
    PROCESSING --> SETTLED: balanced posting
    PROCESSING --> FAILED: processing error
```

## Architecture

```mermaid
flowchart LR
    Client["Client Bank simulator"] --> API["Payment Gateway API"]
    API --> Validation["Validation + idempotency"]
    Validation --> Screening["Synthetic Compliance Service"]
    Screening --> Ledger["Double-entry Ledger"]
    Ledger --> DB[(MySQL)]
    DB --> Reporting["Reconciliation + Reporting Service"]
    API --> Audit["Audit Service"]
    Audit --> DB
```

Version `0.1.0` keeps processing synchronous and deterministic so the complete transaction can be inspected and tested. The next milestone replaces the processing boundary with RabbitMQ, retries, and dead-letter handling.

## Quick start

Requirements: Docker with Docker Compose.

```bash
docker compose up --build
```

Then open:

- Swagger UI: <http://localhost:8080/swagger-ui>
- OpenAPI JSON: <http://localhost:8080/api-docs>
- Health check: <http://localhost:8080/health>

Local Java workflow:

```bash
mvn test
mvn spring-boot:run
```

## Three-minute demo

### 1. Successful settlement

```bash
curl -X POST http://localhost:8080/api/payments \
  -H 'Content-Type: application/json' \
  --data @samples/payment-accepted.json
```

Expected result: `SETTLED`, six status events, and one debit plus one equal credit entry.

### 2. Synthetic compliance rejection

```bash
curl -X POST http://localhost:8080/api/payments \
  -H 'Content-Type: application/json' \
  --data @samples/payment-rejected.json
```

Expected result: `REJECTED` with no ledger entries.

### 3. Duplicate protection

Run the first command again. The API returns HTTP `409 Conflict` because `messageId` is idempotent.

### Batch processing and reconciliation

```bash
curl -X POST http://localhost:8080/api/payment-batches \
  -F 'file=@samples/payment-batch.csv'

curl 'http://localhost:8080/api/reconciliation/daily'
curl 'http://localhost:8080/api/compliance-reports/daily'
```

## API surface

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/payments` | Submit a synthetic payment instruction |
| `GET` | `/api/payments/{id}` | Read state, history, screening, and ledger entries |
| `POST` | `/api/payment-batches` | Upload the documented CSV format |
| `GET` | `/api/payment-batches/{id}` | Inspect batch counts and row errors |
| `GET` | `/api/reconciliation/daily` | Compare daily debit and credit totals |
| `GET` | `/api/compliance-reports/daily` | Return generic status and currency totals |
| `GET` | `/api/audit-events` | Read the latest audit events or filter by aggregate |
| `POST` | `/api/screening-rules` | Add a synthetic screening rule |
| `GET` | `/api/screening-rules` | List configured rules |

## Documentation

- [System design](docs/system-design.md)
- [Interface specification](docs/interface-specification.md)
- [Batch processing flow](docs/batch-processing-flow.md)
- [Data dictionary](docs/data-dictionary.md)
- [Security boundaries](docs/security-boundaries.md)
- [Test scenarios](docs/test-scenarios.md)
- [English client walkthrough](docs/client-walkthrough-en.md)
- [中文详细路线图](docs/roadmap-zh.md)

## 中文项目介绍

BankBridge 是一个面向后端工程实践和求职展示的跨境支付、对账与模拟合规处理沙盒。项目使用 Java 21、Spring Boot、MySQL、Flyway、Docker 和自动化测试，完整建模一笔合成支付指令从接收、校验、规则筛查、复式记账、结算到日终对账与审计追踪的处理过程。

项目重点不在普通 CRUD 页面，而在可靠后端系统中的关键工程问题：

- 使用唯一业务键实现重复请求保护与幂等控制。
- 使用明确状态机记录支付生命周期和不可变状态历史。
- 使用金额相等的借方与贷方分录验证复式记账平衡。
- 对 CSV 批量任务进行逐行错误隔离，避免单条失败影响整批处理。
- 使用 Flyway 管理数据库版本，并通过 OpenAPI、Docker 和 GitHub Actions 提供可复现的开发与验证流程。
- 使用合成数据和模拟规则控制安全边界，不连接真实银行、支付网络或监管基础设施。

当前 `v0.1.0` 采用同步、确定性的处理方式，便于复现、检查和测试每一次状态变化。RabbitMQ、事务发件箱、重试、死信队列、监控和性能基线属于后续路线图，不作为当前已经实现的能力展示。

该项目适合用于 Java 后端、金融科技、测试开发、系统实施、解决方案和英文技术交付岗位的作品展示。

### 三分钟演示

```bash
docker compose up --build
```

服务启动后可访问：

- Swagger UI：<http://localhost:8080/swagger-ui>
- OpenAPI JSON：<http://localhost:8080/api-docs>
- 健康检查：<http://localhost:8080/health>

建议依次演示成功结算、模拟合规拒绝和重复请求冲突三条路径。对应请求样例位于 `samples/`，完整命令见上方英文演示章节。

## Roadmap

- `v0.2`: RabbitMQ, transactional outbox, idempotent consumers, retries, and dead-letter recovery
- `v0.3`: authentication, role-based access, searchable audit data, and report export
- `v0.4`: lightweight operations dashboard, structured logs, metrics, and Grafana
- `v0.5`: performance baselines, resilience tests, deployment, and supply-chain checks
- `v1.0`: stable API, bilingual portfolio documentation, reproducible demo, and recorded walkthrough

The detailed milestones, test requirements, deliverables, acceptance criteria, and suggested ten-week schedule are documented in the [Chinese project roadmap](docs/roadmap-zh.md).

## License

[MIT](LICENSE)
