---
name: java-spring-hex-playbook
description: Use when placing or reviewing responsibilities, dependencies, ports, transactions, events, persistence, Trigger adapters, contracts, or technology choices in an existing Java/Spring application using DDD or hexagonal architecture.
---

# Java Spring Hexagonal Playbook

## Core principle

选择满足当前证据的**最小充分架构**。先识别项目真实边界，再决定职责、端口与机制；每项建议都标记为架构不变量、项目事实、团队默认值、条件方案或示例。

## Decision precedence

`用户明确要求 > 项目代码与 ADR > 已选择架构的不变量 > 团队默认值 > 条件方案 > 示例`

项目现状与目标架构不一致时，同时记录“当前事实、目标规则、影响与迁移条件”。项目选择不同架构时，按真实架构评审，并准确命名其风格。

## Evidence gate

先检查 `ddd-modeling` 的工程交接契约。缺失业务事实会改变职责、事务或一致性方案时，本轮只输出：

1. 已知证据与项目现状。
2. 一个最高影响问题。
3. 该答案会改变的架构决定。

第三部分完成即结束本轮；得到答案后再进入架构流程。

## Workflow

1. 读取现有模块、依赖、代码惯例、`CONTEXT.md`、Context Map、ADR 与工程交接契约。**完成标准：所有影响当前决定的输入均标为项目事实、业务证据或未知。**
2. 为每个相关限界上下文选择 [module-strategies.md](references/module-strategies.md) 中的局部架构，再决定 Maven 模块。**完成标准：每个上下文都有策略名称、证据、最小写路径、最小读路径与升级触发条件。**
3. 沿一个真实用例追踪入口、Application、Domain 与出站适配器，定位每个职责和端口的真正消费者。**完成标准：每个新增或移动的职责都有所有者；每个端口都有消费者、契约和实现边界。**
4. 标出聚合、不变式、事务边界、远程副作用与失败语义。**完成标准：每个事务能指向必须原子成立的规则；未知结果、重试或补偿责任都有所有者或明确缺口。**
5. 查询问题按 [cqrs-decision-ladder.md](references/cqrs-decision-ladder.md) 选择最小充分等级。**完成标准：说明当前等级、一致性窗口、恢复方式、升级证据和停在本级的理由。**
6. 事件问题按 [transactions-and-events.md](references/transactions-and-events.md) 先选事件档位，再加载已检测或用户点名的技术 reference。**完成标准：只应用当前档位的正确性清单；技术机制均有需求证据与替代项。**
7. 设计与风险一一对应的验证。**完成标准：每项关键决定至少有一个可执行检查、测试或指标，且未出现本项目不存在的技术分支。**
8. 输出架构决策包。**完成标准：每项建议都有证据、规则级别、所有者、影响、排除理由和升级触发条件。**

## Hexagonal invariants

以下规则只在项目选择六边形目标架构时成立：

- Domain 保持框架无关，不依赖 Application 或适配器。
- Application 编排用例与事务，并拥有仅由用例消费的出站端口。
- 端口由真正消费者拥有；出站适配器实现端口。
- 入站适配器承载 HTTP、MQ、RPC 与 Job 协议责任；项目使用 `Trigger` 术语时沿用该名称。
- 转换属于被跨越的边界，每个转换器只认识边界两侧类型。
- 一个事务默认修改一个聚合；更强原子性由具名业务不变式举证。
- Domain Event 是内部领域事实，Integration Event 是外部契约；可靠发布需要业务状态与待发布事实的原子交接策略。
- Aggregate Repository 服务写模型，Application Query Port 服务读用例；缓存是独立策略。

## Decision gates

| 提议 | 进入条件 | 当前证据不足时的最小方案 |
|---|---|---|
| 新 Maven 模块 | 编译隔离、独立发布、复用或部署需求 | 单模块包边界 + ArchUnit |
| 新出站端口 | 明确消费者需要替换、隔离或测试某能力 | 在用例内保持直接、局部依赖 |
| Domain Service | 稳定领域名称且行为无自然对象归属 | 聚合行为或 Application 编排 |
| CQRS 升级 | 查询形状、指标、一致性窗口或隔离需求 | 当前阶梯等级 |
| 可靠 Integration Event | 跨边界事实必须最终送达 | 本地事务或直接调用的已知语义 |
| Redis、MQ、RPC、Job 技术 | 已存在该技术或有可观察的非功能需求 | 项目现有机制 |

## Recommendation grammar

每条建议使用以下一种可检查形状：

- **事实 → 决定**：引用项目事实或业务证据，再给出决定。
- **若条件 → 方案**：条件必须是可观察需求、指标或故障语义；条件未证实时保持为条件方案。
- **示例 → 形状**：只说明接口、包或流程的可能形状，并标记为示例。

项目未提供的并发策略、权限、幂等、审计字段、一致性窗口、缓存或消息需求进入“未知/待验证”栏；得到证据后再升级为建议。

## Reference routing

- 依赖方向或框架无关 Domain：读 [architecture-boundaries.md](references/architecture-boundaries.md)。
- Maven 模块、局部架构、API 发布或启动装配：读 [module-strategies.md](references/module-strategies.md)。
- Application Service、Domain Service 或行为放置：读 [application-and-domain.md](references/application-and-domain.md)。
- Repository/Client 所有权、端口、适配器或转换：读 [ports-and-adapters.md](references/ports-and-adapters.md)。
- 查询分离、读模型或 Event Sourcing 等级：读 [cqrs-decision-ladder.md](references/cqrs-decision-ladder.md)。
- 事务、事件语义、可靠投递或 Outbox：读 [transactions-and-events.md](references/transactions-and-events.md)。
- 聚合持久化、重建、查询、分页或缓存：读 [persistence-and-queries.md](references/persistence-and-queries.md)。
- HTTP、MQ、RPC、Job 或入口职责：读 [trigger.md](references/trigger.md)。
- 失败分类、协议映射或数字错误码：读 [errors-and-contracts.md](references/errors-and-contracts.md)。
- 命名：读 [naming-conventions.md](references/naming-conventions.md)。
- 测试与机械守护：读 [testing-and-guardrails.md](references/testing-and-guardrails.md)。
- 项目已使用或用户明确询问某技术时，读取对应的 `tech-mybatis.md`、`tech-redis.md`、`tech-dubbo.md`、`tech-messaging.md`、`tech-jobs.md` 或 `tech-mapstruct.md`。

## Output contract

输出与请求等深的**架构决策包**：

1. 业务证据、项目事实与未知项。
2. 当前局部架构和最小充分方案。
3. 决策账本：建议、规则级别、所有者、证据、影响与升级条件。
4. 用例路径：入口、Application、Domain、端口、适配器与依赖方向。
5. 事务、一致性、失败与事件档位；只列适用分支。
6. 查询相关时，列当前 CQRS 等级、窗口、恢复、升级条件与停止理由。
7. 排除方案、排除证据和需要退回建模的问题。
8. 与风险对应的验证命令、测试或指标。

简单评审可以合并栏目，但规则级别、证据、所有者和升级条件必须保留。示例只展示形状，不升级为项目建议。
