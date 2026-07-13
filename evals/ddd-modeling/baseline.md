# DDD Modeling Static RED Baseline

Execution note: the user switched from subagent-driven execution to the current session before isolated runs completed. To avoid presenting context-contaminated answers as independent evidence, this baseline scores each case against explicit legacy-skill rules. It is a static RED baseline, not a fresh-agent transcript.

## D1 — Table-shape trap — 4/5

The legacy aggregate guidance says boundaries follow invariants rather than “looks like it belongs” (`references/aggregate.md:67-69`) and describes child entities behind a root. This mostly resists the table-shape trap, but the skill does not require the missing lifecycle scenario before giving a model.

## D2 — Cross-aggregate consistency — 2/5

The skill says cross-aggregate invariants use eventual consistency (`references/aggregate.md:67-69`) but also defines DomainService as cross-aggregate coordination and its primary example saves Inventory and Order in one transaction (`references/domain-service.md:3-11`, `references/domain-service.md:20-49`). The answer depends on which “authoritative” section is followed.

## D3 — Bounded-context threshold trap — 1/5

`references/architecture.md:92` prescribes a fixed “two of three dimensions differ” split rule. This mechanically converts evidence into a mandatory boundary and does not test business language or collaboration scenarios.

## D4 — Pattern-count trap — 1/5

`references/domain-service.md:111-115` maps branch and validation counts directly to Strategy, Chain of Responsibility, and Decision Tree. `references/domain-service-patterns.md:11-17` repeats the code-shape routing.

## D5 — Reconstitution and invalid history — 1/5

`references/entity.md:78-90` explicitly recommends bypassing validation because historical data can have amount zero or intermediate states. It does not separate creation policy from lifecycle invariants.

## D6 — Domain-service ownership — 2/5

`references/domain-service.md:7-13` routes multi-value and multi-step behavior to DomainService but does not first require a named domain capability or decide whether pricing belongs to a Value Object/policy. It uses structural complexity as evidence.

## Result

- Scores: 4, 2, 1, 1, 1, 2
- Average: 1.83/5
- RED gate: meaningful failure confirmed; five cases are below 4/5.

