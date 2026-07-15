# Module Strategies

## Core rule

模块数量是隔离手段，不是 DDD 成熟度指标。采用最小可行模块化，出现独立发布、复用、部署或编译期隔离需求时再拆分。

## Package-only profile

小型服务使用一个 Maven 模块，以包结构表达 `domain`、`application`、`trigger`、`infra`，通过 ArchUnit 守护方向。适合团队小、部署单一、依赖图简单的应用。

## Boundary-module profile

需要编译期隔离或独立测试时，拆为：

```text
domain
application
adapters (or trigger + infra)
bootstrap
```

只有变化原因和依赖边界不同，才值得形成独立模块。

## Published-contract profile

对外契约需要独立版本化与发布时增加 `api`。入口需要独立复用、部署或编译期隔离时拆出 `trigger`。`starter/bootstrap` 仅在承担独立装配职责时存在。

原六模块结构可以作为强化隔离示例：

```text
api / domain / application / infra / trigger / starter
```

它是团队默认方案之一，不是 DDD 或六边形架构的必要条件。

## Local architecture by bounded context

先为每个限界上下文选择局部架构，再决定它落在哪些 Maven 模块。模块边界与上下文内部的建模深度是两个不同决定。

| 局部架构 | 适用信号 | 设计重点 | 不需要的复杂度 |
| --- | --- | --- | --- |
| 简单 CRUD / Transaction Script | 规则少、字段编辑直接、生命周期简单 | Application 直接编排校验、数据访问与本地事务；查询按用例返回 DTO | 不强造聚合行为、值对象层级或领域事件 |
| 富领域模型 / Hexagonal | 存在明确不变式、行为和生命周期 | 聚合保护即时一致性；Application 编排用例和事务；端口由消费者拥有 | 不把规则复制到 Controller、Job、Mapper 或数据库脚本 |
| 独立查询 / CQRS Read Side | 列表、报表或查询形状与写模型明显不同 | 按查询用例组织读模型，直接通过 Query Port 返回，不为读取重建聚合 | 不因读模型不同自动引入异步投影、独立读库或 Event Sourcing |

同一个部署单元可以同时包含三种局部架构；六模块不要求每个上下文内部复制六层。

选择时明确写出当前策略名称、业务证据、最小写路径、最小读路径、测试重点和升级触发条件。简单上下文后来出现跨字段不变式或复杂生命周期时，可以演进为富领域模型；查询压力或一致性需求变化时，再单独评估 CQRS 等级。

## Decision questions

1. 哪个边界需要编译器阻止非法依赖？
2. 哪个产物需要独立发布或版本兼容？
3. 哪个入口或适配器需要独立部署/替换？
4. 新模块带来的构建、测试和认知成本是否值得？

## Verification

- 用 `mvn dependency:tree` 检查实际模块依赖。
- 用 Enforcer 验证禁止依赖。
- 单模块方案用 ArchUnit 验证包方向。
- 删除一个假想模块后若没有边界损失，优先不创建它。

## Common mistakes

- 小项目一开始固定六模块。
- 把包名变化当作限界上下文拆分。
- 为每种技术建立一个 Maven 模块。
- `api` 同时包含契约和应用实现。
