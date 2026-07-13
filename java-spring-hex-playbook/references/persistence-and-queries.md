# Persistence and Queries

## Aggregate Repository

Repository 以领域语言提供加载和保存聚合的能力，例如：

```java
interface OrderRepository {
    Optional<Order> findById(OrderId id);
    void save(Order order);
}
```

不要把 DO、Mapper、分页 UI 字段或缓存 API 暴露给 Domain。`save`、`add/update` 是否分离取决于 ID 生成、ORM 与并发模型，不设全局铁律。

## Query Port

列表、分页、报表和详情查询由 Application 拥有 Query Port，直接返回用例读模型，不为读取重建聚合：

```java
interface OrderQueryPort {
    PageResult<OrderSummary> findPage(OrderPageCriteria criteria);
}
```

读模型不应反向进入聚合写路径。

## Transaction ownership

Repository 实现默认不自行开启业务事务。Application 用例控制事务，以便保存聚合、Outbox 和其他同一原子边界内的数据。适配器可使用数据库级原子语句，但不要偷偷扩大用例事务。

## Reconstruction

- 跳过 ID 生成、创建事件和创建期副作用。
- 保持所有生命周期不变式。
- 历史数据不兼容时，迁移、隔离、版本化重建或显式建模 Legacy 状态。
- 不因“存量可能无效”而绕过全部校验。

优先使用领域生成 ID 或受控工厂。若持久化生成 ID，避免为了回填而公开任意业务代码都能调用的 `bindId()`/`bumpVersion()`；可返回保存后的状态或使用受限重建边界。

## Cache options

缓存不是 Repository 的固有职责。可选方案：

| 方案 | 适用 | 风险 |
|---|---|---|
| Repository decorator | 对聚合加载透明缓存 | 一致性与对象快照复杂 |
| Query adapter cache | 缓存读模型 | 允许一定陈旧时更简单 |
| Explicit cache port | 用例明确需要缓存语义 | 增加编排责任 |
| No cache | 数据库满足目标 | 最低复杂度 |

失效时机要结合事务提交：事务内先删缓存可能被并发请求以旧数据库值回填；可采用提交后失效、版本化 key、短 TTL、CDC 或业务可接受的策略。不要把“写时立即删除”声明为无条件正确。

## Optimistic concurrency

版本检查属于持久化并发控制，冲突应映射为可识别的用例/领域失败。重试必须重新加载并重新执行规则，不要对已变更的内存聚合盲目重复 SQL。

## Verification

- Repository contract test 验证聚合 round-trip 与并发冲突。
- Query adapter integration test 验证 SQL、排序和游标。
- 缓存测试覆盖提交/回滚、并发回填和 TTL。
- ArchUnit 禁止 Domain/Application import DO/DAO/Redis 类型。

