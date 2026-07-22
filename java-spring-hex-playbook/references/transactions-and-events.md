# Transactions and Events

## Decision sequence

1. 命名正在修改的聚合与必须守护的不变式。**完成标准：每个原子性要求都有具名业务规则。**
2. 画出本地状态、远程副作用与提交时点。**完成标准：每个失败点都能说明已发生的效果与责任方。**
3. 选择本地 Domain Event、可靠 Integration Event 或 Event Sourcing 档位。**完成标准：事件事实来源、消费者边界与可靠性需求明确。**
4. 只应用所选档位的清单，并选择满足它的最小机制。**完成标准：正确性需求、机制、替代项和验证一一对应。**

默认一个数据库事务只修改一个聚合；同库多聚合原子修改必须举证即时一致性。聚合记录技术无关的 Domain Event。同进程弱可靠后续动作可使用 Spring 事务事件；跨上下文可靠交付可使用 Outbox，也可采用能提供同等原子交接与恢复证据的项目机制。

## Choose the event profile first

先按业务语义分类，再**按事件语义裁剪**审查项。Event Sourcing 的清单不得自动施加给普通本地事件；Outbox 也不是看到 Domain Event 就必须使用的默认设施。

### 本地 Domain Event

适用于同一进程内的领域事实和协调，尤其是允许弱可靠性或可从事实源修复的派生工作。只检查：

- 事实名称和事实所有者是否属于通用语言。
- 事件来自哪个聚合以及哪个已成功行为。
- Domain Event 是否技术无关，不携带 Spring、MQ 或持久化类型。
- 是否在业务事务活跃时收集/发布，以及提交后处理失败是否影响业务正确性。

若跨进程不可丢失，升级为可靠 Integration Event；若完整事件流不是事实来源，不套用 Event Sourcing 的快照、回放和 schema 清单。

### 可靠 Integration Event

适用于跨上下文或跨进程且要求最终送达的事实。除本地事实语义外，必须检查：

- 外部契约所有者、契约版本和兼容策略；内部 Domain Event 类不得直接冻结为长期外部契约。
- 业务状态与 Outbox 的原子写入，以及 relay 的领取、检查点/发布进度、重试和积压监控。
- `aggregateId` 等分区键或顺序键，只承诺业务真正需要的局部顺序。
- 消费者幂等键（`consumer + eventId` 或稳定业务键），且幂等记录与业务效果同事务提交。
- at-least-once 下发布成功但标记未知、消费成功但 ack 未知等未知结果。
- 可恢复错误的退避重试、不可恢复错误的 DLQ、人工/自动回放和契约不兼容处理。

**Outbox 不是终点**：写入 Outbox 只完成本地原子性，relay、broker、消费者、幂等、顺序、进度与失败恢复仍需端到端设计。

### Event Sourcing

仅当事件流本身是聚合事实来源并且审计、时态查询或重放价值足以覆盖成本时采用。必须检查：

- 每个流的事件版本、`expectedVersion` 乐观并发和命令幂等。
- 事件应用函数确定、无外部副作用，回放幂等并能识别缺口或未知版本。
- 投影的幂等键、检查点、乱序/重复策略、监控和投影重建流程。
- 快照只是条件化性能优化，不是事实来源；无快照仍应能从头重建。
- schema 演进、upcaster/兼容读取、历史迁移和旧事件契约测试。
- 查询投影的一致性窗口、写后读策略，以及回放和长期事件存储的性能与运维成本。

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

