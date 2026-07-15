# Baseline–candidate comparison

## Run summary

| Run | Skill commit | Score | Critical failures |
| --- | --- | ---: | ---: |
| Baseline | `b809ab78b9c9aa6b1483d7fbdd4aba6fb67db82e` | 49.5/52 (95.19%) | 0 |
| Candidate | `f4f69ab61b57899761283681cd6d2205d9dc7078` | 51/52 (98.08%) | 0 |

Both runs used fresh isolated runners. A runner received only the target skill,
the case prompt, and raw fixture facts. It did not receive the rubric, expected
or forbidden behavior, design rationale, baseline answers, or another run's
output. A separate evaluator received only the corpus, rubric, and raw responses.

## Per-case behavior

| Case | Baseline | Candidate | Observed change |
| --- | ---: | ---: | --- |
| `db-table-to-aggregate` | 3/3 | 2.5/3 | Still rejects schema-derived boundaries and marks candidates unverified, but the candidate response did not make the requested command plus success/failure evidence list as explicit as the baseline. |
| `ambiguous-account-contexts` | 5.5/6 | 6/6 | Relationship ledger now includes explicit translation and failure responsibility; the small directional map and hotspot list remain clear. |
| `order-payment-aggregate` | 4.5/5 | 5/5 | The engineering handoff now consistently exposes all nine fixed slots, including context goal, query window, authority/translation, hotspots, and prohibited freeze items. |
| `simple-catalog-crud` | 4/5 | 4.5/5 | Now explicitly names the local strategy as simple CRUD/Transaction Script and avoids six-module directory narration; project-level compile/assembly wording remains slightly implicit. |
| `order-lifecycle-rich-domain` | 5/5 | 5/5 | Unchanged strength: rich-domain/Hexagonal strategy, aggregate invariants, Application transaction ownership, adapter separation, and focused tests. |
| `simple-pagination-cqrs` | 5/5 | 5/5 | Unchanged strength with a stable level-2 recommendation, observable upgrade triggers, and explicit rejection of higher levels. |
| `operations-report-cqrs` | 4.5/5 | 5/5 | The level-4 recommendation now explicitly rejects both separate storage and Event Sourcing while retaining checkpoint, rebuild, cost, and stop rules. |
| `local-domain-event` | 4/4 | 4/4 | Unchanged strength: local profile only, weak-reliability acceptance, transaction timing, and no automatic Outbox/Event Sourcing checklist. |
| `integration-event` | 7/7 | 7/7 | Unchanged strength: versioned external contract, Outbox atomicity, ordering, idempotency, progress, retry/DLQ, unknown outcomes, and at-least-once semantics. |
| `event-sourcing` | 7/7 | 7/7 | Unchanged strength: concurrency, deterministic replay, projection recovery, conditional snapshots, schema evolution, and consistency window. |

## Regressions and remaining weaknesses

- The database-table case lost 0.5 because evidence discovery was described but
  not rendered as an explicit command/success/failure checklist. It remains safe:
  no schema-only boundary was asserted and no critical behavior occurred.
- The simple catalog case still describes the six modules as project-level
  physical structure without spelling out both “compile” and “assembly” in one
  direct sentence.
- No candidate response triggered a critical forbidden behavior.

## Publication decision

Publish. The candidate passes all deterministic contracts, scores 98.08% (above
the 90% gate), has zero critical failures, and improves the fixed decision
artifacts that motivated this change. The isolated regression is non-critical
and is documented for a later evaluation refinement rather than hidden by
editing raw responses.
