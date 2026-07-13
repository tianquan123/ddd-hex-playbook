# Errors and Contracts

## Internal categories

| 类别 | 含义 | 示例 |
|---|---|---|
| Domain rejection | 业务规则拒绝操作 | `OrderNotPayable`, `InsufficientStock` |
| Use-case conflict | 幂等、并发或流程冲突 | `RequestAlreadyProcessed`, optimistic conflict |
| External dependency failure | 超时、网络、熔断、下游不可用 | `PaymentUnavailable` |
| Unexpected system failure | 编程错误或未知基础设施故障 | 记录并隐藏内部细节 |

Domain 使用领域名称，不携带 HTTP 状态、Dubbo 异常或 Facade 数字码。不要禁止所有 JDK 异常：编程契约错误与可预期业务拒绝需要区分。

## Protocol mapping

每种 Trigger 显式映射内部失败：

- HTTP：状态码 + 稳定错误 body。
- RPC：版本化错误契约或声明异常。
- MQ：ack/retry/DLQ 决策，不向消息发送者返回 HTTP 响应。
- Job：可重试/不可重试分类与告警。

`GlobalExceptionHandler` 只覆盖 HTTP，不覆盖 RPC、MQ 和 Job。

## Numeric error codes

数字码段可以作为团队默认的外部契约，例如按上下文分段，但不能成为 Domain 字面量。建立显式映射：

```text
OrderNotPayable -> ORDER_STATE_NOT_PAYABLE(1101) -> HTTP 409
```

映射表由对外契约或 Trigger 拥有，并用测试保证完整性与唯一性。不要仅凭千位/百位自动推断所有 HTTP 状态。

## Remote failures

远程业务拒绝、超时、连接失败、熔断和未知响应必须分开建模：

- 业务拒绝通常不可通过技术重试改变。
- 超时结果可能未知，非幂等写不能假定失败。
- 连接失败可能安全重试，但仍受幂等和预算约束。
- `catch(Throwable)` 会吞掉编程错误与进程级问题，应避免。

## Messages and security

- 对外错误消息稳定且不泄漏堆栈、SQL、下游地址或敏感字段。
- 日志保留内部原因、traceId 与异常链。
- 字段级校验详情是否对外返回由契约和安全要求决定，不设全局强制。

## Verification

- 映射测试覆盖每个公开错误码且 code 唯一。
- HTTP/RPC contract test 验证不同协议映射。
- 超时与未知结果测试验证不会把非幂等写误报成功/失败。
- 未知异常测试验证外部不泄漏内部细节。
