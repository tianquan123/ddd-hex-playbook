---
name: ddd-modeling
description: Use when deciding or reviewing bounded contexts, ubiquitous language, entities, value objects, aggregates, invariants, domain services, or domain events, especially when business evidence is incomplete or a model is being inferred from code or database structure.
---

# DDD Modeling

## Core principle

建立一条可追溯的**证据链**：业务场景与语言 → 不变式与时间边界 → 上下文与聚合候选 → 失败探针 → 建模决策。证据尚未通过探针时，结论保持为候选。

## Select a track

- 已有具体场景或现有模型需要审查：走**快速评审轨**。
- 业务事实不足、术语冲突或职责边界不清：走**发现建模轨**。
- 只问定义且不需要项目判断：直接回答。

缺失答案会改变边界、职责或一致性方案时，一次提出一个最高影响问题。

### Blocking-question response

当该答案会改变用户请求中的核心结论时，本轮响应只包含三个部分：

1. **已知证据**：列出可直接证实的事实和项目现状。
2. **一个问题**：提出最高影响问题。
3. **为何阻塞**：列出答案会改变的模型决定。

第三部分完成即结束本轮。得到答案后再进入建模轨；用户明确要求比较方案时，改为输出等权条件分支。

## Fast review track

1. 读取现有模型、代码、`CONTEXT.md`、Context Map 与 ADR；把每条输入标为业务事实、项目现状、团队默认值或未验证假设。**完成标准：影响当前结论的输入全部有来源与状态。**
2. 提取场景、业务时间边界、通用语言、不变式和一致性窗口。**完成标准：每个候选边界都能指向至少一条业务证据。**
3. 用失败、并发、超时、重复、乱序或补偿场景挑战候选。**完成标准：每个保留候选都有通过的探针；未通过项降级为候选或排除。**
4. 输出建模决策包。**完成标准：每个结论标明“已验证、候选、已排除或未知”，且排除项和未知项均附影响。**

## Discovery track

1. 按 [discovery-evidence.md](references/discovery-evidence.md) 建立证据时间线。**完成标准：覆盖业务目标、成功、失败和关键时间边界；缺口显式列为热点。**
2. 澄清通用语言、事实权威、不变式，以及即时、延迟和补偿规则。**完成标准：每条关键规则都有示例、反例、权威来源和允许的一致性窗口。**
3. 提出最小的上下文、聚合和事件候选，并运行失败探针。**完成标准：候选均有证据链与探针结果，不以表、框架或团队数量单独定边界。**
4. 上下文候选稳定后，按 [context-mapping.md](references/context-mapping.md) 生成覆盖当前问题的小地图。**完成标准：方向、事实权威、契约、翻译和失败责任都有值或明确热点。**
5. 按 [engineering-handoff.md](references/engineering-handoff.md) 形成工程交接契约。**完成标准：所有已验证决定、候选、禁止固化项与验证责任都已交接，且交接内容只含业务语义、正确性边界和实现约束。**

## Decision rules

| 问题 | 决定性证据 | 候选通过条件 |
|---|---|---|
| Entity 或 Value Object | 身份、连续性、可替换性 | 相等性与生命周期语义可解释 |
| Aggregate 边界 | 具名业务不变式与一致性窗口 | 最小边界能原子守护即时规则 |
| Bounded Context | 语言含义、规则与事实权威 | 跨边界翻译和责任可说明 |
| Domain Service | 稳定领域名称与行为归属 | 行为无自然对象归属且不是应用编排 |
| Domain Event | 已发生且值得关注的业务事实 | 生产者、发生时数据与业务反应可说明 |

## Output contract

需要项目判断时输出与当前问题等深的**建模决策包**：

1. 场景范围、业务目标与时间边界。
2. 证据账本：已验证事实、项目现状、默认值和未验证假设。
3. 通用语言、歧义、事实权威与热点。
4. 不变式及即时、延迟或补偿的一致性要求。
5. 上下文、聚合、实体/值对象、服务与事件候选及探针结果。
6. 决策账本：已验证、候选、已排除、未知；每项附证据或影响。
7. 需要实现交接时，追加工程交接契约；只规定业务语义和正确性边界。

简单评审可以合并栏目，但每个直接影响结论的未知项、排除证据和决策状态必须保留。

## Reference routing

- 需要建立证据时间线、示例、反例或热点时，读 [discovery-evidence.md](references/discovery-evidence.md)。
- 需要判断语言或上下文边界时，读 [bounded-contexts.md](references/bounded-contexts.md)。
- 需要判断上下文关系、事实权威、翻译或失败责任时，读 [context-mapping.md](references/context-mapping.md)。
- 需要判断身份、相等性、值语义或重建时，读 [entities-and-value-objects.md](references/entities-and-value-objects.md)。
- 需要判断聚合、不变式或一致性窗口时，读 [aggregates-and-invariants.md](references/aggregates-and-invariants.md)。
- 行为归属不清时，读 [domain-services.md](references/domain-services.md)。
- 需要判断领域事实的含义和命名时，读 [domain-events.md](references/domain-events.md)。
- 领域决定需要交给 Java/Spring 设计时，读 [engineering-handoff.md](references/engineering-handoff.md)。

## Evidence gates

- 数据库表、外键、事务、类名、框架和部署形态只记录为**项目现状**；它们需要业务证据才能升级为模型决定。
- 缺失事实写入**未知/热点**，并指出它会改变的决定；阻塞核心结论时使用“证据摘要 → 单一问题 → 受影响决定”的固定输出。
- 用户只要求分析或评审时，交付报告；文件修改需要单独授权。
- 建模产物保持框架中立；Java 包、Spring 事务、消息与持久化机制交给 `java-spring-hex-playbook`，并随附工程交接契约。
