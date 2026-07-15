---
name: ddd-modeling
description: Use when deciding or reviewing bounded contexts, ubiquitous language, entities, value objects, aggregates, invariants, domain services, or domain events, especially when a model is being inferred from code or database structure.
---

# DDD Modeling

## Core principle

从具体业务场景、通用语言、身份、生命周期、不变式与一致性需求推导模型。场景尚未验证前，任何边界都只是候选方案。

## Track selection

- 已有具体场景或现有模型需要审查：使用**快速评审轨**。
- 业务事实不足、术语冲突或需要划分上下文：使用**发现建模轨**。
- 缺失答案会改变模型时，一次只追问一个问题。

## Fast review track — 快速评审轨

1. 读取现有模型、代码、`CONTEXT.md`、Context Map 与 ADR。
2. 提取已有场景、不变式和一致性边界。
3. 用反例挑战实体、聚合和上下文划分。
4. 输出保留、调整、被排除的设计及证据。

## Discovery track — 发现建模轨

1. 按 [discovery-evidence.md](references/discovery-evidence.md) 建立发现证据。
2. 澄清通用语言、不变式及即时、延迟和补偿规则。
3. 提出上下文、聚合和事件候选，并用失败场景挑战。
4. 候选上下文稳定后，按 [context-mapping.md](references/context-mapping.md) 生成只覆盖当前问题的小地图和关系表。
5. 按 [engineering-handoff.md](references/engineering-handoff.md) 形成工程交接契约。

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

需要项目判断时输出**建模决策包**；只保留与当前问题有关的内容：

1. 场景范围与业务时间边界。
2. 证据、示例、反例与热点。
3. 通用语言及歧义。
4. 不变式和即时、延迟或补偿的一致性要求。
5. 上下文、聚合及事件候选。
6. 被排除方案、排除证据与未验证假设。
7. 按参考模板输出工程交接契约；不规定 Java 包和框架。

简单评审可压缩篇幅，但不能把未知项写成事实，也不能省略直接影响结论的排除证据和未验证假设。

## Reference routing

- 业务发现、示例、反例或热点：读 [discovery-evidence.md](references/discovery-evidence.md)。
- 上下文边界或语言冲突：读 [bounded-contexts.md](references/bounded-contexts.md)。
- 上下文关系、事实权威、翻译或失败责任：读 [context-mapping.md](references/context-mapping.md)。
- 实体、值对象、身份、相等性或重建：读 [entities-and-value-objects.md](references/entities-and-value-objects.md)。
- 聚合边界、不变式或一致性：读 [aggregates-and-invariants.md](references/aggregates-and-invariants.md)。
- 行为没有自然对象归属：读 [domain-services.md](references/domain-services.md)。
- 领域事实的含义和命名：读 [domain-events.md](references/domain-events.md)。
- 交给 Java/Spring 架构评审前：读 [engineering-handoff.md](references/engineering-handoff.md)。

## Stop conditions

不要从数据库表推导聚合，不要发明缺失事实，不要按固定数量拆上下文，不要按分支数量选设计模式，也不要把框架实现当作领域证据。用户只要求分析或评审时，不自动修改 `CONTEXT.md`、Context Map、ADR 或项目代码。建模产物不规定 Java 包和框架；剩余问题若是 Java/Spring 代码放置，把工程交接契约交给 `java-spring-hex-playbook`。

