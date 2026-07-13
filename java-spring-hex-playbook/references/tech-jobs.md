# Scheduled Job Options

> Load this reference only when the project already uses this technology or the user explicitly asks about it. The choices below are implementation options, not DDD invariants.

## When to use

业务需要时间触发、批处理、超时扫描或外部调度。是否采用 XXL-JOB、Elastic-Job、Quartz 或平台调度由部署环境决定。

## Recommended default

- Job Trigger 只解析调度参数、分片和运行元数据，然后调用 Application 用例。
- 用例按业务键幂等，允许任务重跑。
- 批处理使用稳定游标和小事务，记录进度与失败项。
- 集群单实例执行只有在业务确实要求时才加 leader/lock；平台已保证单实例则不重复加锁。

## Risks and alternatives

- 固定 TTL 锁可能在长任务中提前过期。
- 扫描任务可能因 OFFSET、时钟和重复执行漏数/重数。
- 事件驱动可以替代部分轮询任务。
- 裸 `@Scheduled` 对单实例简单任务可能已经足够。

## Verification

- 测试重复触发、并发触发、进程中断和恢复游标。
- 锁方案测试 token 所有权、续期和误释放。
- 记录每批处理数量、耗时、失败原因与可重放标识。

