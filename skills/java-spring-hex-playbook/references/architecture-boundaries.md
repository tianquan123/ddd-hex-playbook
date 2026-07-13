# Architecture Boundaries

## Rule levels

本文件的依赖方向是选择六边形架构后的不变量；具体模块名、工具和包名是默认值或实现选项。

## Framework-independent Domain

Domain 不依赖 Spring、ORM、缓存、消息、RPC/HTTP SDK，也不依赖 Application、Trigger、Infra。允许不改变领域语义、生命周期和测试方式的通用工具库、静态工具类及编译期工具。

判断一个依赖是否合适：

- 是否让领域 API 暴露技术类型？
- 是否要求容器、数据库或网络才能创建/测试领域对象？
- 是否由框架控制对象生命周期或状态转换？

任一答案为“是”，通常不应进入 Domain。

## Dependency direction

| 组件 | 可以依赖 | 不应依赖 |
|---|---|---|
| Domain | JDK、获准通用库、自己拥有的领域端口 | Application、Trigger、Infra、技术 SDK |
| Application | Domain、自己拥有的用例端口 | Trigger DTO、Infra 实现 |
| Trigger | Application 输入输出、协议库 | 具体持久化/缓存实现 |
| Infra | Domain/Application 定义的出站端口、技术库 | Trigger |
| API（可选） | 契约所需的最小库 | 业务实现与 Infra |
| Bootstrap | 各适配器与配置 | 业务规则 |

端口由消费者拥有，而不是为了画出统一箭头一律放入 Domain。

## Enforcement

Maven 模块依赖用 Enforcer 守护，规则名必须是：

```xml
<bannedDependencies>
  <excludes>
    <exclude>org.springframework:*</exclude>
    <exclude>org.mybatis:*</exclude>
  </excludes>
</bannedDependencies>
```

`excludes` 是被禁止的依赖；按项目依赖图补充清单并验证传递依赖。

ArchUnit 与 Maven 互补：

| 风险 | 验证方式 |
|---|---|
| 模块 POM 引入禁止依赖 | Maven Enforcer |
| 同模块包之间错误 import | ArchUnit |
| 运行时装配缺失 | Spring context/integration test |
| 领域对象依赖容器 | 纯单元测试直接 `new` |

## Common mistakes

- 把“框架无关”解释为禁止所有第三方工具。
- 只靠 Code Review 守护可自动验证的依赖。
- 只用 Maven 检查包内调用方向。
- 让 Bootstrap 或 Configuration 承载业务判断。

