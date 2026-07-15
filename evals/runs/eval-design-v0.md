# Evaluation design v0 — rejected control

- Date: 2026-07-15
- Skill commit: `b809ab78b9c9aa6b1483d7fbdd4aba6fb67db82e`
- Result: 10/10 cases, 100%, 0 critical failures

This run was rejected as an evaluation-design failure, not accepted as a useful
baseline. The original cases were too easy and the rubric allowed unstructured
prose to count as equivalent to named deliverables, so the control run exposed
no behavioral gap.

The replacement baseline strengthens the cases around fixed decision artifacts:
small Context Maps, relationship tables, engineering handoff slots, explicit
local architecture selection, a numbered six-level CQRS ladder, and explicit
event-profile classification. The rubric now scores a required named artifact
as missing when the response only provides prose without the requested shape.
