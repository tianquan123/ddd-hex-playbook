---
name: java-spring-hex-playbook
description: Use when placing or reviewing responsibilities, dependencies, ports, transactions, events, persistence, Trigger adapters, contracts, or technology choices in an existing Java/Spring application using DDD or hexagonal architecture.
---

# Java Spring Hexagonal Playbook

## Core principle

在保持明确边界的同时适配已经存在的项目。每条建议必须标明它是架构不变量、团队默认值、条件化方案还是示例。

## Priority

`用户明确要求 > 项目代码与 ADR > 架构不变量 > 团队默认值 > 条件化方案 > 示例`

如果项目决定违反这里的架构不变量，指出其影响，并按项目真实架构工作；不要偷偷把项目描述成另一种架构。

## Workflow

1. 读取现有模块、依赖、惯例、`CONTEXT.md` 与 ADR。
2. 判断问题是否仍是领域建模；若是，先使用 `ddd-modeling`。
3. 找到真正消费该职责或端口的层。
4. 追踪依赖方向、事务与一致性影响。
5. 只读取当前问题和已检测技术对应的 reference。
6. 返回建议、规则级别、所有者、依赖影响、替代方案和验证方法。

## Architecture invariants

- Domain 框架无关，不依赖 Application、Trigger 或 Infra。
- Application 编排用例、控制事务，并拥有仅由用例消费的出站端口。
- 端口由真正消费者拥有；Infra 实现出站端口。
- Trigger 统一承载 HTTP、MQ、RPC、Job 入站适配器。
- 转换属于被跨越的边界；不存在认识所有层类型的全能 Convertor。
- 默认一个事务修改一个聚合；更强耦合必须举证。
- DomainEvent 不是技术消息；可靠跨边界投递使用 IntegrationEvent 与 Outbox。
- Aggregate Repository、Application Query Port 与缓存策略相互分离。

## Reference routing

- 依赖方向或框架无关 Domain：读 [architecture-boundaries.md](references/architecture-boundaries.md)。
- Maven 模块数量、API 发布、Trigger 拆分或启动装配：读 [module-strategies.md](references/module-strategies.md)。
- Application Service、DomainService 或行为放置：读 [application-and-domain.md](references/application-and-domain.md)。
- Repository/Client 所有权、出站端口、适配器或转换：读 [ports-and-adapters.md](references/ports-and-adapters.md)。
- 事务、聚合一致性、Spring 事件、IntegrationEvent 或 Outbox：读 [transactions-and-events.md](references/transactions-and-events.md)。
- 聚合持久化、重建、查询模型、分页或缓存：读 [persistence-and-queries.md](references/persistence-and-queries.md)。
- HTTP、MQ、RPC、Job、传输去重或入口职责：读 [trigger.md](references/trigger.md)。
- 领域失败、依赖失败、协议映射或数字错误码：读 [errors-and-contracts.md](references/errors-and-contracts.md)。
- 类、包、数据库、Redis Key 或适配器命名：读 [naming-conventions.md](references/naming-conventions.md)。
- 单元、架构、集成、事务或失败路径测试：读 [testing-and-guardrails.md](references/testing-and-guardrails.md)。
- MyBatis Mapper、XML、DO 或 TypeHandler：读 [tech-mybatis.md](references/tech-mybatis.md)。
- Redis 缓存、锁或 Key：读 [tech-redis.md](references/tech-redis.md)。
- Dubbo Provider、Client、超时或重试：读 [tech-dubbo.md](references/tech-dubbo.md)。
- 消息发布、消费、重试、顺序或 DLQ：读 [tech-messaging.md](references/tech-messaging.md)。
- 定时任务、分片、集群执行或分布式锁：读 [tech-jobs.md](references/tech-jobs.md)。
- MapStruct mapper 所有权、注入或生成代码验证：读 [tech-mapstruct.md](references/tech-mapstruct.md)。

## Output contract

- 推荐方案
- 规则级别
- 所属层或端口消费者
- 依赖与一致性影响
- 未选择的方案及原因
- 可执行的验证命令或测试

## Common mistakes

- 先套目录，再理解职责。
- 把示例技术当成架构要求。
- 为保持依赖箭头而把所有端口塞进 Domain。
- 用一个转换器连接所有层。
- 用本地事务掩盖跨聚合或远程失败语义。

