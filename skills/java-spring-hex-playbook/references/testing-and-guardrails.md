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

## Failure-path tests

- 业务事务回滚时没有可投递 Outbox。
- broker 不可用后 relay 可以恢复。
- 同一消息重复消费只有一次业务效果。
- 远程调用超时与未知结果不会被当作业务拒绝。
- 缓存失效与并发回填不破坏约定的一致性窗口。
- 乐观锁冲突重新加载并重放规则，而非盲重试 SQL。

## Verification commands

按项目实际工具使用：

```text
mvn test
mvn verify
mvn dependency:tree
```

输出必须无失败；警告需判断是否暴露架构守护未执行、生成代码缺失或测试被跳过。

