# Trigger

Trigger（六边形架构中的 inbound adapters）统一承载进入应用的 HTTP、MQ、RPC 和 Job 入口。保留这一项目术语，并按协议分包。

```text
trigger/
├── http/
├── mq/
├── rpc/
├── job/
└── shared/   # 仅放确实跨协议的入口横切能力
```

## Protocol responsibilities

| Trigger | 合法职责 |
|---|---|
| HTTP | 路由、Header/Token 解析、格式校验、Request/Response 映射、HTTP 状态 |
| MQ | 反序列化、ack/nack、重投元数据、分区/顺序键、传输去重 |
| RPC | 契约兼容、RPC 异常/结果映射、协议元数据 |
| Job | 调度参数、分片、misfire/并发策略、触发 Application 用例 |

这些是协议逻辑，不是领域规则。Trigger 可以不止“一行转发”，但不能决定价格、库存资格或订单状态转换。

## Idempotency

区分两层：

- **传输去重**：同一消息 delivery 是否已经处理，可留在 MQ Trigger/消息基础设施。
- **业务幂等**：同一业务请求是否允许重复产生效果，属于 Application 用例，因为 HTTP、MQ、RPC、Job 都可能触发同一用例。

Application 定义幂等存储端口，Infra 实现。Trigger 不直接注入 `RedisService` 来代替业务幂等。

## Error handling

HTTP advice 只处理 HTTP；RPC、MQ、Job 分别使用自己的协议错误处理。Trigger 把内部失败映射为协议契约，不把 HTTP 状态写回 Domain。

## Verification

- 同一业务键从 HTTP 与 MQ 各调用一次，只有一个业务效果。
- MQ 重投测试同时覆盖 ack、幂等记录和业务事务失败。
- ArchUnit 禁止 `trigger..` 依赖 `infra..` 实现包。
- Trigger 单元/切片测试验证转换和协议状态，不重复测领域规则。

## Common mistakes

- HTTP Controller 与 MQ Listener 各自实现一套业务幂等。
- Trigger 直接操作 Redis、DAO 或 RepositoryImpl。
- 把参数格式校验和业务资格校验混为一谈。
- 假设 HTTP 全局异常处理能覆盖 RPC/MQ。
