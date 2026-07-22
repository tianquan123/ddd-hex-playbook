# Testing and Guardrails

## Test by boundary

| 边界 | 主要测试 | 不应需要 |
|---|---|---|
| Domain | 纯单元测试：不变式、状态转换、值语义 | Spring、数据库、网络 |
| Application | 用例测试：fake/mock ports、事务结果、失败协调 | HTTP、真实外部系统 |
| Persistence adapter | SQL/映射/round-trip 集成测试 | Controller |
| Trigger | 协议切片/契约测试 | 重复测试领域规则 |
| Integration event | Outbox、重复、延迟、顺序、失败恢复 | exactly-once 假设 |

## Architecture guardrails

- Maven Enforcer `<bannedDependencies>`：模块级依赖。
- ArchUnit：同模块包/import 方向。
- 编译测试：API 与实现隔离、MapStruct 生成代码。
- Spring context test：最终装配、Bean 唯一性与配置条件。

不要只靠人工 Code Review 守护可机械验证的规则。

## Failure-path routing

只加载当前设计实际存在的失败分支：

- 使用 Outbox：验证业务回滚无可投递记录，broker 不可用后 relay 可恢复。
- 消费 Integration Event：验证重复、延迟和乱序下只有一次业务效果。
- 调用远程写：验证超时与未知结果按契约收敛。
- 使用缓存：验证失效与并发回填满足一致性窗口。
- 使用乐观锁：验证冲突后重新加载并重放业务规则。
- 仅本地 CRUD：验证本地事务原子性、SQL 映射、并发覆盖和协议错误映射。

## Verification commands

按项目实际工具使用：

```text
mvn test
mvn verify
mvn dependency:tree
```

输出必须无失败；警告需判断是否暴露架构守护未执行、生成代码缺失或测试被跳过。
