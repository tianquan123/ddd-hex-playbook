# Redis Options

> Load this reference only when the project already uses this technology or the user explicitly asks about it. The choices below are implementation options, not DDD invariants.

## When to use

只有明确的延迟、吞吐、跨实例协调或短期状态需求，并已定义可接受陈旧窗口时使用 Redis。

## Recommended default

- 优先缓存 Query read model，而非可变聚合快照。
- Key 声明业务、上下文、用途、身份、TTL 与最大基数。
- 缓存通过 decorator/adapter 实现，不把 Redis 类型暴露给端口。
- 分布式锁使用唯一 token、原子 compare-and-delete 与租约/续期；能用数据库唯一约束时不先上锁。

## Risks and alternatives

- 穿透：短 TTL 空值或 Bloom filter。
- 击穿：single-flight、逻辑过期或预热。
- 雪崩：TTL 抖动与容量保护。
- 事务内删缓存可能被旧值回填；考虑提交后失效、版本 key、短 TTL 或 CDC。
- 本地缓存、数据库索引或不缓存可能更简单。

## Verification

- 覆盖命中、未命中、回源失败、回滚和并发回填。
- 验证 key 基数、TTL、序列化兼容与大 key。
- 锁测试覆盖过期、误释放、进程暂停和重复请求。

