---
name: ddd-modeling
description: Use when deciding or reviewing bounded contexts, ubiquitous language, entities, value objects, aggregates, invariants, domain services, or domain events, especially when a model is being inferred from code or database structure.
---

# DDD Modeling

## Core principle

从具体业务场景、通用语言、身份、生命周期、不变式与一致性需求推导模型。场景尚未验证前，任何边界都只是候选方案。

## Workflow

1. 如果存在，先读 `CONTEXT.md`、上下文映射、ADR 与相关代码。
2. 明确参与者、命令、成功结果、失败结果和业务时间边界。
3. 标出歧义术语；缺失答案会改变模型时，一次只追问一个问题。
4. 写出业务不变式，并区分必须即时一致与允许延迟/补偿的规则。
5. 提出限界上下文、实体、值对象、聚合、领域服务和领域事件候选。
6. 用反例和边界场景挑战候选，再作推荐。
7. 输出推荐、证据、被排除方案、假设与实现影响。

## Direct-answer exception

不需要项目判断的定义题直接回答，不要为了回答术语问题而审问用户。

## Quick reference

| 问题 | 主要证据 |
|---|---|
| 实体还是值对象 | 身份、连续性、可替换性 |
| 聚合边界 | 必须即时成立的不变式 |
| 限界上下文 | 语言含义、规则与事实权威 |
| 领域服务 | 有领域名称但无自然对象归属的能力 |
| 领域事件 | 业务上已经发生且值得关注的事实 |

## Output contract

- 建议结论
- 支撑结论的场景与不变式
- 被排除的候选及排除证据
- 假设与尚未解决的问题
- 不规定 Java 包和框架的实现影响

## Reference routing

- 上下文边界或语言冲突：读 [bounded-contexts.md](references/bounded-contexts.md)。
- 实体、值对象、身份、相等性或重建：读 [entities-and-value-objects.md](references/entities-and-value-objects.md)。
- 聚合边界、不变式或一致性：读 [aggregates-and-invariants.md](references/aggregates-and-invariants.md)。
- 行为没有自然对象归属：读 [domain-services.md](references/domain-services.md)。
- 领域事实的含义和命名：读 [domain-events.md](references/domain-events.md)。

## Stop conditions

不要从数据库表推导聚合，不要按固定数量拆上下文，不要按分支数量选设计模式，也不要把框架实现当作领域证据。剩余问题若是 Java/Spring 代码放置，把建模结论交给 `java-spring-hex-playbook`。

