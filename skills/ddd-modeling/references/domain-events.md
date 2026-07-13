# Domain Events

## Meaning

DomainEvent 是领域中已经发生、对业务有意义且值得其他参与者关注的事实。使用过去时和通用语言命名，例如 `OrderAccepted`，而不是 `OrderRowInserted`。

## Model the event

1. 明确发生了什么业务事实。
2. 明确哪个聚合或领域行为产生该事实。
3. 只携带理解事实所需、发生时已经确定的业务数据。
4. 识别潜在消费者及其业务反应，但不让生产者依赖消费者。
5. 讨论重复、延迟和顺序对业务含义的影响。

## DomainEvent versus command

- Command 表达希望发生的动作，可能被拒绝。
- DomainEvent 表达已经发生的事实，不应以命令式名称伪装。
- “保存完成”“缓存删除”是技术通知，除非业务确实赋予其领域含义。

## Boundary note

本技能只判断事件的领域语义。Spring 事件、MQ、重试、Outbox、IntegrationEvent 与事务提交时机属于 `java-spring-hex-playbook`。不要因为需要 MQ 就倒推一个 DomainEvent，也不要让领域模型直接依赖技术发布器。

## Common mistakes

- 用 CRUD 或表名命名事件。
- 把未来意图写成过去事实。
- 事件携带完整可变聚合快照。
- 为了异步而制造没有业务意义的“领域事件”。
- 在建模阶段承诺 exactly-once 等技术语义。
