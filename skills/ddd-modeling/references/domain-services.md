# Domain Services

## Ownership test

DomainService 表达一个有明确领域名称、无自然 Entity 或 Value Object 归属、通常无状态的领域能力。

依次判断：

1. 该行为是否自然改变某个聚合并守护其不变式？若是，放聚合。
2. 是否是值之间的计算或转换？若是，优先值对象或领域函数。
3. 业务人员是否能给这个跨对象能力一个稳定名称？若否，可能只是应用编排。
4. 它是否主要负责调用顺序、事务、远程副作用或错误协调？若是，放 Application。

## Appropriate examples

- 根据多个领域值计算税费的 `TaxPolicy`。
- 在多个候选资源之间做领域调度决策的 `Scheduler`。
- 无法自然属于单个账户的汇率换算规则。

## Not evidence

- 方法很长。
- 有三个以上 Repository。
- 有七个校验或很多 if-else。
- 需要事务或远程调用。

这些信号说明代码需要理解或重构，不证明存在领域服务。

## Patterns

只有领域中真实存在可替换规则、组合规格或状态转换策略时才引入 Strategy、Specification 等模式。`Filter`、`Node`、`DynamicContext`、`Factory` 等技术词若不能被业务语言解释，不应进入 Domain。

## Common mistakes

- 把 DomainService 当作跨 Repository 的事务脚本。
- 为避免给聚合增加行为而创建贫血服务层。
- 按分支数量机械选择责任链或决策树。
- 让 DomainService 负责消息发布、重试或 DTO 转换。

