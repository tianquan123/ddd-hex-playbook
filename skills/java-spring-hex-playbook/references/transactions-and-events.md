# Transactions and Events

## Decision sequence

1. 命名正在修改的聚合与必须守护的不变式。
2. 默认一个数据库事务只修改一个聚合。
3. 同库多聚合原子修改必须举证即时一致性，并解释为何不能调整边界或补偿。
4. 远程调用不进入数据库事务；调用顺序由失败语义决定。
5. 聚合只记录 DomainEvent，不依赖 Spring/MQ publisher。
6. 同进程、允许弱可靠性时，在事务仍活跃时发布 Spring event，由 `@TransactionalEventListener(AFTER_COMMIT)` 响应。
7. 跨上下文或要求可靠投递时，业务数据与 Outbox 同事务写入，relay 发布 IntegrationEvent，消费者保持幂等。

## Event types

| 类型 | 含义 | 可靠性/技术 |
|---|---|---|
| DomainEvent | 领域中已经发生的事实 | 由聚合记录，技术无关 |
| In-process event | 同进程协调 | 可使用 Spring event；默认非持久化 |
| IntegrationEvent | 对其他上下文发布的稳定契约 | 通常由 DomainEvent 转换 |
| Outbox record | 待可靠投递的持久化记录 | 与业务状态同事务提交 |

不要把同一个 Java 类未经转换直接兼任内部 DomainEvent 和长期外部契约。

## Valid Spring flow

```text
active transaction
  -> save aggregate
  -> publish Spring event while transaction is active
  -> commit
  -> @TransactionalEventListener(AFTER_COMMIT)
```

禁止首次在 `TransactionSynchronization.afterCommit()` 中发布事件，再期待默认 `@TransactionalEventListener(AFTER_COMMIT)` 绑定已结束事务。事务外发布默认不会触发该 listener，除非显式启用 fallback，而 fallback 也不提供可靠消息语义。

## Reliable integration flow

```text
Application transaction
  -> save aggregate
  -> map DomainEvent to IntegrationEvent
  -> insert Outbox record
  -> commit

Outbox relay
  -> claim unpublished records
  -> publish to broker
  -> mark published / retry

Consumer
  -> deduplicate by eventId or business key
  -> apply business transaction
```

Relay 与 broker 之间通常仍是 at-least-once；消费者幂等不可省略。

## Remote calls and local state

“先调远程还是先落库”没有统一答案：

- 先远程：本地失败时需要补偿已发生的外部效果。
- 先落库：远程失败时需要保留 `PENDING/FAILED` 状态并重试。
- 非幂等写必须使用业务幂等键和明确的未知结果处理。

## Verification

- 回滚业务事务后不应存在可投递 Outbox。
- 提交成功但 broker 暂停时，Outbox 仍可稍后投递。
- relay 重复发布时，消费者只有一次业务效果。
- Spring 事件测试验证发布发生在活跃事务内。

