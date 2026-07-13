---
name: "ddd-hex-playbook"
description: "Use when 用户在 Java/Spring Boot 多模块工程中问 DDD（domain-driven design）与六边形架构问题：分层职责与依赖方向、类/包命名、聚合根/实体/值对象/Repository/Client/DomainService/AppService/Trigger 的设计与代码放置位置、领域事件、CQRS、事务边界、错误码、测试策略与反模式。不负责工程脚手架创建（见 ddd-project-creator）。"
---

# DDD Engineering Guidelines

当用户询问 DDD 架构、分层职责、命名规范、领域建模、代码放置位置或实现模式时使用本技能。

## 使用方式

**第一步**：按「按问题类型路由」判断读哪篇参考文档；高频一问一答已内联在下方「Quick Reference」，可直接回答，无需打开参考文档。

### 按问题类型路由

| 用户问 | 先读 |
|---|---|
| "这个类/这段代码放哪层？" | [architecture.md](references/architecture.md) + 对应层文档 |
| "Xxx 该是聚合根、实体还是值对象？" | [aggregate.md](references/aggregate.md) / [entity.md](references/entity.md) / [value-object.md](references/value-object.md) |
| "逻辑放 DomainService 还是 AppService？" | [domain-service.md](references/domain-service.md) + [application-service.md](references/application-service.md) |
| "大量 if-else / 串行校验 / 嵌套 switch 怎么整？" | [domain-service-patterns.md](references/domain-service-patterns.md) |
| "远程调用（Dubbo/HTTP）怎么接？" | [client.md](references/client.md) |
| "缓存、DO、Mapper 怎么做？" | [repository.md](references/repository.md) + [infrastructure.md](references/infrastructure.md) |
| "错误码怎么编？" | [naming.md「错误码编号策略」](references/naming.md#错误码编号策略) |
| "怎么命名 Xxx？" | [naming.md](references/naming.md) |
| "领域事件怎么发？" | [aggregate.md「领域事件发布」](references/aggregate.md#领域事件发布) |

### 核心工程规范（按主题）

| 主题 | 文档 |
| --- | --- |
| 架构概览 | [references/architecture.md](references/architecture.md) |
| 项目结构 | [references/project-structure.md](references/project-structure.md) |
| 命名规范 | [references/naming.md](references/naming.md) |
| 实体设计 | [references/entity.md](references/entity.md) |
| 聚合根设计 | [references/aggregate.md](references/aggregate.md) |
| 值对象设计 | [references/value-object.md](references/value-object.md) |
| 仓储模式 | [references/repository.md](references/repository.md) |
| 远程服务调用 | [references/client.md](references/client.md) |
| 领域服务 | [references/domain-service.md](references/domain-service.md) |
| 领域服务设计模式参考库（策略/责任链/决策树，按需查阅） | [references/domain-service-patterns.md](references/domain-service-patterns.md) |
| 应用服务编排 | [references/application-service.md](references/application-service.md) |
| 触发层设计 | [references/trigger-layer.md](references/trigger-layer.md) |
| 基础设施层 | [references/infrastructure.md](references/infrastructure.md) |

## Quick Reference（高频一问一答，本节为权威）

### 依赖方向（权威）

`Trigger → API(facade) → Application → Domain ← Infra`，`Starter` 装配 trigger + infra。

- `starter` → trigger + infra
- `trigger` → facade(api) + application（不直达 infra）
- `application` → domain **only**（不依赖 facade）
- `infra` → domain（实现 Repository / Client 接口）
- `domain` → **零外部依赖**（禁 Spring / MyBatis / Redis / MQ / HTTP，由 maven-enforcer 守护）

### 层间入参契约（权威）

数据形态沿调用链逐层翻译，**Command 只活在 application 层**：

| 边界 | 翻译 | 产物 |
| --- | --- | --- |
| Trigger → AppService | `facade Request → 领域 Command` | `{Xxx}Command`（application 层） |
| AppService → Domain | `Command → 领域值对象 / 强类型 ID / 原语` | 聚合方法 / DomainService / Repository 只收领域词汇，**绝不收 Command** |
| Repository | `save(聚合根)` / `findById(强类型 ID)` | 聚合根或 ID |

> domain 不依赖 application，故 DomainService / 聚合方法入参**不能是 Command**；参数多时在 domain 层定义不可变输入 record（按领域角色命名，不叫 Command）。

### 模块 ↔ 包名

| 层 | artifactId | 包名 |
| --- | --- | --- |
| 对外发布层（Facade） | `{project}-api` | `facade` |
| 领域层 | `{project}-domain` | `domain.{context}` |
| 应用编排层 | `{project}-application` | `application.{context}` |
| 基础设施层 | `{project}-infra` | `infra.{context}` |
| 触发器层 | `{project}-trigger` | `trigger` |
| 启动模块 | `{project}-starter` | 根包 |

### 类后缀速查

| 概念 | 命名 | 归属 |
| --- | --- | --- |
| 聚合根 | `{Domain}Aggregate` | domain |
| 实体 | `{Domain}Entity` | domain |
| 值对象 / 强类型 ID | 裸名 `{Xxx}` / `{Domain}Id` | domain |
| 领域服务 | `{Xxx}DomainService(Impl)` | domain（零 Spring，infra `@Bean` 装配） |
| Repository | `{Xxx}Repository(Impl)` / `{Xxx}CacheRepository` / `{Tech}{Xxx}Repository` | 接口 domain / 实现 infra |
| Client | `{Xxx}Client` / `{Tech}{Xxx}Client` | 接口 domain / 实现 infra |
| 应用服务 | `{Xxx}AppService`（无接口、无 Impl） | application |
| 应用命令 | `{Xxx}Command` | **application**（非 domain） |
| Convertor | `{Xxx}Convertor`（MapStruct 静态单例 `INSTANCE`） | application / infra |
| Controller / Listener / Job / Provider | `{Xxx}Controller` 等 | trigger |
| 视图 / 契约 / 持久化对象 | `{Xxx}VO` / `{Xxx}DTO` / `{Xxx}DO` | trigger / facade / infra |

### Impl 后缀规则

✅ 领域服务实现 `{Xxx}DomainServiceImpl`、Repository 默认实现 `{Xxx}RepositoryImpl`；❌ AppService（无接口）、Client（用 `{Tech}` 前缀）。

### 错误码（纯数字，按上下文分千位段）

| 码段 | 上下文 |
| --- | --- |
| 1000-1999 | order |
| 2000-2999 | payment |
| 3000-3999 | inventory |
| 4000-4999 | customer |
| 9000-9999 | 通用 / 系统（`UNKNOWN_ERROR=9000`、`PARAM_INVALID=9001`、`UNAUTHORIZED=9003`） |

每段内部再按类型细分：`x000-x099` 校验、`x100-x199` 状态、`x200-x299` 不存在、`x300-x399` 冲突（order 乐观锁 = `1301`）、`x400-x499` 外部依赖。新增上下文认领新千位段，**禁止跨段借用**。完整规则见 [naming.md](references/naming.md)。

### Convertor 方法名

`Command/DO → Aggregate` = `toAggregate`；`Aggregate → DO` = `toDO`；`→ Response` = `toResponse`；`→ VO` = `toVO`；`Facade Request → Command` = `toCommand`。`toDomain` 已废弃。

## 核心规则

- Domain 层表达核心业务规则，**零框架依赖**（maven-enforcer 守护，详见 [architecture.md](references/architecture.md)）；不直接依赖 Spring / 数据库 / 缓存 / 外部系统。
- Domain 定义 Repository 和 Client 接口，Infra 实现这些接口（依赖倒置）。
- Trigger 层只负责接收请求、参数校验、`Request → Command` 转换和转发，不写业务逻辑。
- Application 层负责编排跨领域/跨服务的业务流程、事务边界、DTO 转换；**不含业务规则**。
- Infra 只处理持久化、缓存、外部服务调用和技术适配；domain 的 Bean（含领域服务、策略/Filter/Node）由 infra `@Configuration` + `@Bean` 装配，保证 domain 零 Spring。
- API 层（模块名 `{project}-api`，包名 `facade`）= 对外发布的版本化契约（Dubbo 接口、DTO、错误码、常量），零 Spring 依赖，独立发布 JAR。
- 事务边界 = 聚合边界；**远程调用禁止进入 `@Transactional`**。

## 约束

- 不负责创建工程；创建工程交给 `ddd-project-creator` 技能。
- 示例中的包名和类名应按用户项目上下文替换，不绑定任何特定个人、品牌或组织名称。
