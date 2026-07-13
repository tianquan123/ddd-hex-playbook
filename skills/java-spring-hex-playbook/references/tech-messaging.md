# Messaging Options

> Load this reference only when the project already uses this technology or the user explicitly asks about it. The choices below are implementation options, not DDD invariants.

## When to use

跨上下文解耦、可接受异步、需要缓冲峰值或可靠集成事件时。

## Recommended default

- 可靠发布使用业务事务 + Outbox，而非数据库提交后直接双写 broker。
- IntegrationEvent 是版本化外部契约，不直接复用内部聚合对象。
- 消费者按 `eventId` 或业务键幂等，并在同一业务事务记录处理结果。
- 按业务 key 分区以获得必要顺序；不要承诺全局顺序。
- 重试分类可恢复/不可恢复错误，耗尽进入 DLQ 并可观测。

## Risks and alternatives

- at-least-once 意味着重复是常态。
- 长重试会阻塞分区；毒消息要隔离。
- 事件 schema 演进需要向后/向前兼容策略。
- 同进程简单后续动作可用事务事件，不必先引入 broker。

## Verification

- 数据库提交成功但 broker 不可用时仍可恢复投递。
- 重复、乱序、延迟和 DLQ 回放不会产生重复业务效果。
- Contract test 验证 schema 兼容与敏感字段。

