# Application and Domain

## Responsibility test

| 责任 | 所有者 | 规则级别 |
|---|---|---|
| 聚合自身状态变化与不变式 | Aggregate/Entity/Value Object | 架构不变量 |
| 有领域名称但无自然对象归属的能力 | DomainService/Policy | 条件化方案 |
| 用例顺序、事务、端口调用和失败协调 | Application Service | 架构不变量 |
| 协议解析与响应映射 | Trigger | 架构不变量 |

## Domain behavior

能自然归属聚合、实体或值对象的行为优先放回模型。DomainService 只有在业务能稳定命名该能力、且它没有自然对象归属时成立。

以下不是 DomainService 的成立证据：

- 方法长、步骤多或 if-else 多；
- 依赖多个 Repository；
- 需要事务或远程调用；
- 想复用一段应用流程。

## Application behavior

Application Service：

- 接收用例输入并加载所需领域对象；
- 调用领域行为；
- 控制事务边界；
- 调用自己拥有的出站端口；
- 协调外部副作用、重试决策和失败状态；
- 返回 Application Output。

它不返回 Trigger 的 `ApiResponse`/VO，不接收持久化 DO，也不把领域规则写成 setter 编排。

## Cross-aggregate work

Application 可以协调多个聚合的业务流程，但默认每个事务只修改一个聚合。DomainService 不应成为“加载多个 Repository、在一个事务全部保存”的默认事务脚本。若需要跨聚合即时一致，先用 `ddd-modeling` 明确不变式并举证例外。

## Verification

- 纯 Domain 测试能否不启动 Spring？
- Application 测试能否用 fake/mock ports 验证调用顺序与失败状态？
- Application 公共签名是否出现 Request、VO、DO、`ApiResponse` 或技术 SDK 类型？

## Common mistakes

- 所有逻辑都放 `XxxAppService`，产生贫血模型。
- 所有复杂逻辑都放 `DomainService`，产生领域垃圾桶。
- 为复用用例编排而让 DomainService 管事务和远程调用。
- 让 Application 知道 HTTP、MQ 或数据库行结构。

