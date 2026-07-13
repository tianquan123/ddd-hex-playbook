# DDD Modeling Current-Session GREEN Evaluation

Execution note: these answers were produced and scored in the current implementation session at the user's request. They verify rule coverage and output shape but are not independent-context evidence.

## D1 — Table-shape trap — 5/5

仅凭 `order` 与 `order_item` 两张表不能判断聚合。先确认 OrderItem 是否有独立于订单的身份与生命周期，以及哪些规则必须通过 Order 同步守护。若条目只能随订单创建、修改和删除，且订单总额/条目非空必须即时成立，它通常是 Order 聚合内实体；若条目能独立交易和演进，才考虑独立聚合。数据库表拆分只是持久化选择。

Evidence: `SKILL.md` requires missing scenario questions; `entities-and-value-objects.md` uses identity/lifecycle; `aggregates-and-invariants.md` rejects table-derived boundaries.

## D2 — Cross-aggregate consistency — 5/5

“建单时减库存”描述流程，不足以合并 Order 与 Inventory。先写不变式：库存不能为负、订单只有获得预留后才能接受；再确认允许的短暂状态和补偿。通常保留两个聚合，以预留、事件和状态推进协作。若业务证明两者必须在同库即时原子成功且不能补偿，可把多聚合事务作为显式例外，并重新审视边界。

Evidence: `aggregates-and-invariants.md` defaults to one aggregate modification and requires evidence for the exception.

## D3 — Bounded-context threshold trap — 5/5

不能因“团队不同 + 术语略有不同”机械判定必须拆分，也不能因共享事务就合并。需要比较 Order 与 Payment 对关键术语的定义、规则修改权、事实权威和跨边界失败责任，再提出边界候选。共享事务可能只是当前技术耦合。

Evidence: `bounded-contexts.md` treats team and transaction boundaries as evidence, not a numeric formula.

## D4 — Pattern-count trap — 5/5

七个校验和多个 if 不能决定 DDD 模式。先区分它们是聚合不变式、可组合业务规格、可替换政策，还是应用流程校验；保持简单代码，只有领域真实存在可替换规则或组合语义时才考虑 Strategy/Specification。执行顺序和共享上下文也需由业务语义说明，而非由数量触发责任链。

Evidence: `domain-services.md` explicitly rejects branch-count and validation-count heuristics.

## D5 — Reconstitution and invalid history — 5/5

`reconstitute` 不应笼统绕过校验。新订单金额为正可能是创建策略，也可能是生命周期不变式：若任何有效订单都不能为零，历史数据应迁移、隔离、版本化重建或显式建模 Legacy 状态；若仅新建受限，重建可跳过创建策略，但仍守护所有生命周期不变式。

Evidence: `entities-and-value-objects.md` separates creation policy from lifecycle invariants.

## D6 — Domain-service ownership — 5/5

信息仍不足以唯一归类。若 Pricing 是业务认可的、跨多个值计算且无自然实体归属的能力，可建模为 `PricingPolicy`/DomainService；产品自身价格不变式仍留在聚合或值对象。Application 负责获取客户等级、活动资格和当前时钟等输入并调用该能力，不把远程调用与事务编排塞进 DomainService。

Evidence: `domain-services.md` requires a named domain capability and keeps use-case orchestration in Application.

## Result

- Scores: 5, 5, 5, 5, 5, 5
- Average: 5.0/5
- GREEN threshold: passed in current-session conformance review.
- Limitation: fresh independent-agent forward testing remains desirable before external deployment.
