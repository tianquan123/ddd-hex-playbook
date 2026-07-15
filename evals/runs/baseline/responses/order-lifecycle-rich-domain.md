推荐把生命周期状态和约束放在 `Order` 聚合内，由 Application 用例加载聚合并调用其行为；HTTP 与定时任务只是两个不同的 Trigger，共用同一套 Application 用例和领域规则。

### 责任落位

| 责任 | 建议落位 | 规则级别 |
|---|---|---|
| 确认后数量不可修改 | `Order.changeQuantity(...)` 的状态守卫 | 架构不变量：聚合自身不变式属于 Domain |
| 发货后不可取消 | `Order.cancel(...)` 的状态守卫 | 架构不变量 |
| 确认、取消、发货的合法状态转换 | `Order.confirm()`、`cancel()`、`ship()` | 架构不变量 |
| 加载订单、调用行为、保存订单、控制事务 | Application Service | 架构不变量 |
| HTTP 路由、请求校验和状态码映射 | HTTP Trigger | 架构不变量 |
| 调度参数、分片、misfire、扫描游标 | Job Trigger | 架构不变量 |
| 查询待关闭订单 | Application 拥有的 Query Port，Infra 实现 | 架构不变量 |
| 聚合加载与保存 | `OrderRepository` 端口，Infra 实现 | 架构不变量 |

领域接口可以类似：

```java
final class Order {
    void changeQuantity(OrderLineId lineId, Quantity quantity) {
        ensureQuantityCanChange();
        // 修改数量并继续守护订单不变式
    }

    void confirm() {
        // 校验当前状态及确认前置条件
        status = OrderStatus.CONFIRMED;
    }

    void cancel(CancelReason reason) {
        if (status == OrderStatus.SHIPPED) {
            throw new OrderCannotBeCancelledAfterShipment();
        }
        // 处理重复取消或其他非法状态
        status = OrderStatus.CANCELLED;
    }

    void ship() {
        // 校验发货前置状态
        status = OrderStatus.SHIPPED;
    }
}
```

这里的代码只是示例；状态名称和可达路径应服从项目现有通用语言。确认前允许调数量、确认后数量冻结、发货后禁止取消，必须由聚合方法自身保证，不能依赖调用者“记得先判断”。

### Application 与 Trigger

Application 可按用例提供 `ChangeOrderQuantity`、`ConfirmOrder`、`CancelOrder`、`ShipOrder`。每个用例在事务中：

1. 按订单身份加载聚合。
2. 调用对应领域行为。
3. 保存聚合。
4. 把领域失败返回为 Application 层可识别的失败。

HTTP Controller 只把请求转换为 Application Input，再把“已发货不能取消”等失败映射为合适的 HTTP 响应；它不能直接判断订单状态，也不能操作 DAO。

定时关闭任务应位于 Job Trigger：解析调度参数、使用稳定游标分批找到候选订单，然后调用与其他入口相同的关闭/取消用例。任务可以筛选候选，但“此订单此刻能否关闭”仍必须由聚合最终裁决，因为扫描结果可能已经过期。批处理采用小事务、记录游标和失败项，并允许重复或并发触发。

如果“超时关闭”和“用户取消”在业务上含义不同，应建成不同用例及领域行为，例如 `closeBecauseExpired(now)` 与 `cancel(reason)`，而不是为了复用而把两者都变成无语义的 `updateStatus`。

### 并发与一致性

HTTP 确认、HTTP 发货和定时关闭可能并发修改同一订单，因此持久化适配器应使用版本字段进行乐观并发控制。版本冲突必须重新加载订单并重新执行领域规则，不能对旧内存对象盲目重试保存。

一个事务默认只修改一个 `Order` 聚合。若成功转换需要可靠通知其他上下文，聚合记录 `OrderConfirmed`、`OrderCancelled` 或 `OrderShipped` 等 Domain Event；Application 在保存订单的同一事务中转换并写入 Outbox，再由 relay 发布 Integration Event。Domain 不依赖 Spring publisher 或 MQ。

### 不建议的方案

- 不把状态判断散落在 HTTP Controller、Job 或各 Application Service 中，否则不同入口会产生不同规则。
- 不提供公共 `setStatus`、`setQuantity` 让外部任意改状态；应通过具名领域行为表达意图。
- 当前规则自然属于订单聚合，不需要仅因存在多个分支就创建 DomainService。
- 数据库约束和乐观锁可以补强并发正确性，但不能替代“哪些状态转换合法”的领域规则。
- 状态机框架只是条件化实现选项；只有生命周期显著增长且项目已有相应约定时再考虑，不能让框架状态枚举取代领域行为。

### 尚需确认的业务问题

现有事实还没有说明“是否只有确认后的订单才能发货”“取消后是否允许再次确认”“重复确认、重复取消、重复发货是幂等成功还是业务失败”。这些答案会改变状态转换图，应在实现前明确；在此之前不要自行开放这些路径。

### 验证

- 纯 Domain 单元测试覆盖：确认前可调数量、确认后调数量失败、发货后取消失败，以及每个非法状态转换。
- Application 测试验证加载—调用—保存顺序、事务回滚和领域失败传播。
- 并发测试覆盖发货与定时关闭竞争、确认与调整数量竞争；冲突方重新加载后必须重新校验。
- Job 测试覆盖重复触发、并发触发、进程中断、游标恢复和候选状态过期。
- HTTP 切片测试只验证请求转换、状态码和错误契约，不重复测试领域规则。
- 使用 ArchUnit/Maven Enforcer 禁止 Domain 依赖 Spring、Trigger 或 Infra，并执行项目实际使用的 `mvn test`、`mvn verify`。
