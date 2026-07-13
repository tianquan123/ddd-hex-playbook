# Java Spring Hexagonal Static RED Baseline

Execution note: this is a source-evidence baseline recorded in the current session after isolated subagent execution was cancelled by user request. It does not claim fresh-agent independence.

## J1 — Small service modules — 1/6

`SKILL.md:70-84` and `references/project-structure.md:3-46` present the six-module layout as the standard structure without a minimum-modularity decision. No rule level or verification method is stated.

## J2 — Outbound port ownership — 1/6

`SKILL.md:119-120` and `references/client.md:3-18` require every Client interface in Domain. They do not ask which layer consumes the payment operation.

## J3 — Boundary conversion — 0/6

`references/application-service.md:103-116` defines one `OrderConvertor` that knows Facade Request, Application Command, Aggregate, Infra DO, Response, and Trigger VO. This contradicts the declared dependency direction.

## J4 — Reliable event delivery — 1/6

`references/aggregate.md:118-136` publishes for the first time in an `afterCommit` callback and then uses `@TransactionalEventListener(AFTER_COMMIT)`. The event is no longer published inside the transaction to which the listener must bind. No transactional Outbox is provided for reliable MQ delivery.

## J5 — Query and cache — 0/6

`references/repository.md:19-27` mixes aggregate persistence with `PageResult<OrderSummary>`, while `references/repository.md:64-73` mandates cache-aside inside `RepositoryImpl`. Query port and cache policy are not separate responsibilities.

## J6 — Trigger idempotency — 1/6

`references/trigger-layer.md:64-76` injects `RedisService` directly into the MQ Trigger even though the root skill states Trigger does not depend on Infra. It treats transport deduplication as the whole idempotency policy.

## J7 — Existing ADR override — 0/6

The legacy skill has no rule-precedence model and labels six modules and static MapStruct `INSTANCE` as authoritative defaults (`SKILL.md:46-115`, `references/application-service.md:103-119`). It gives no mechanism for an ADR to override them.

## Result

- Scores: 1, 1, 0, 1, 0, 1, 0
- Average: 0.57/6
- RED gate: meaningful failure confirmed; every case is below 5/6.
