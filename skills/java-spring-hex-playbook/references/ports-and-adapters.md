# Ports and Adapters

## Consumer-owned ports

端口属于提出需求的消费者，不属于“固定某一层”。

| 需求 | 端口所有者 | 示例 |
|---|---|---|
| 按聚合语义加载/保存 | Domain | `OrderRepository` |
| 用例调用支付、通知、外部流程 | Application | `PaymentPort`, `NotificationPort` |
| 领域规则需要一个外部事实 | Domain（仅语义化接口） | `ExchangeRateProvider` |
| 分页、列表、报表 | Application | `OrderQueryPort` |

Infra 实现这些出站端口。不要为了保持统一依赖图而把 Payment、短信、消息和查询接口全部塞进 Domain。

## Adapter responsibilities

- 入站适配器把 HTTP/MQ/RPC/Job 协议数据转换为 Application Input。
- 持久化适配器把 Aggregate 与 Persistence Model 相互转换。
- 外部服务适配器把 External DTO/异常翻译为内部类型。
- Bootstrap 选择并装配实现，不包含业务判断。

外部 DTO、DAO、Redis 类型和 RPC 异常不得穿过适配器进入 Domain。

## Boundary-owned translation

转换由拥有边界的一侧实现：

```text
HTTP Request   --Trigger mapper-->       Application Input
Application Input --assembler-->         Domain values/commands
Aggregate      --persistence mapper-->   DO/row
External DTO   --ACL translator-->       Internal result/failure
Application Output --Trigger mapper-->   HTTP/RPC Response
```

不创建同时认识 Request、Command、Aggregate、DO、Response、VO 的全能 Convertor。`Mapper`、`Assembler`、`Translator` 按职责命名；MapStruct 只是局部实现工具。

## Failure translation

适配器捕获技术异常并映射为端口契约中声明的失败。不要 `catch(Throwable)` 后把远程业务拒绝、超时和编程错误全部包装成同一种异常。

## Verification

- 用 ArchUnit 禁止 Domain/Application import Trigger/Infra 包。
- 编译 Application 测试时不引入 Web、MyBatis 或 Dubbo 依赖。
- 对每个外部适配器测试 DTO、状态码和异常翻译。

## Common mistakes

- 端口按实现技术命名，如 `DubboPaymentPort`。
- Application 直接调用 DAO 或外部 SDK。
- Domain 接口返回外部系统 DTO。
- 使用全局静态转换器连接所有模块。

