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
