# MyBatis Options

> Load this reference only when the project already uses this technology or the user explicitly asks about it. The choices below are implementation options, not DDD invariants.

## When to use

项目需要显式 SQL、稳定查询性能、复杂读模型或已有 MyBatis 基础设施时。

## Recommended default

- Mapper/DAO 与 DO 放持久化适配器。
- Aggregate ↔ DO 映射留在持久化边界。
- 复杂查询通过 Application Query Port 返回读模型。
- XML 或注解按项目约定选择；不要把 MyBatis 注解放进 Domain。
- 单表 DO 是常用默认值，不代表一张表一个领域实体或聚合。

## Risks and alternatives

- 全量 `SELECT *` 与隐式映射容易在列变化时漂移。
- 深分页优先 keyset/cursor，而非无限 OFFSET。
- TypeHandler 适合稳定单列值映射；多列值对象通常由 mapper/assembler 组合。
- JPA、jOOQ 或原生 JDBC 可能更适合不同查询/对象模型，架构边界不应绑定 MyBatis。

## Verification

- 用真实数据库或 Testcontainers 验证 SQL、索引、时区和事务。
- 对 Aggregate round-trip、乐观锁和空值语义做 contract test。
- ArchUnit 禁止 Domain/Application import MyBatis 与 DO 类型。

