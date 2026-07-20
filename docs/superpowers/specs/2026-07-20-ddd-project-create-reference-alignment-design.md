# DDD Project Create 通用规范模板重构设计

日期：2026-07-20

## 1. 背景

`E:\ddd\zhujing-knowledge-center` 是团队实际使用的 Java/Spring DDD 六模块项目。当前
`ddd-project-create` 已经能够安全、确定性地生成六模块工程，但内置 `SampleOrder` 示例较小，尚未完整表达团队在真实项目中采用的包名、类名、职责边界、HTTP/RPC 双入口、读写分离、持久化适配和显式装配约定。

本次重构以知识中心项目为证据基础，抽取可泛化的工程规范。模板不复制知识中心业务，也不引入团队私有父 POM、公共组件、仓库地址或运行环境配置。

## 2. 目标与非目标

### 2.1 目标

1. 保持现有生成器输入、确认流程、安全策略和退出码兼容。
2. 用一个可删除、可运行、可测试的 `sampleorder` 完整纵向切片表达团队规范。
3. 固化各模块内的包名、类名、角色后缀、转换边界和存放位置。
4. 同时演示 HTTP 与 Apache Dubbo RPC 两种入站适配器。
5. 同时演示 Command 写模型和 Query 读模型，避免 API DTO、Domain Model、View 与 DO 混用。
6. 使用 MyBatis Mapper 接口与 XML 实现正式持久化适配器，使用 H2 完成独立测试。
7. 通过 Python 契约测试、Java 单元测试和 Maven 构建验证规范，而不只依赖文档说明。

### 2.2 非目标

- 不复制知识中心的 Market、Goods、Question、Shelf、Skill 或 Promotion 业务。
- 不引入团队私有父 POM、公共返回组件、Nexus、Nacos 或环境地址。
- 不加入 Redis、MQ、Outbox、定时任务或异步投影。
- 不生成通用 `BaseEntity`、`BaseService` 或 `BaseRepository`。
- 不支持动态选择模块、ORM、RPC 框架或技术版本。
- 不把工程创建器扩展成根据任意实体持续生成 CRUD 代码的二级脚手架。

## 3. 方案选择

考虑过三种方案：

1. **可执行规范模板**：用完整示例切片固化规范，以测试和校验器保护边界。
2. **参考项目裁剪副本**：直接裁剪知识中心的一条业务链路并替换业务名。
3. **元数据驱动代码生成器**：根据上下文和实体元数据持续生成各层代码。

采用方案 1。方案 2 容易携带历史兼容代码与业务偶然性；方案 3 显著扩大生成器职责。可执行规范模板能在保持创建流程简单的同时，为团队提供可编译、可查阅、可验证的命名与放置标准。

## 4. 兼容性约束

生成器继续接收：

```text
create_project.py
  --project-name <projectName>
  --group-id <groupId>
  --base-package <basePackage>
  --parent-dir <parentDirectory>
```

- `basePackage` 仍默认等于 `groupId`。
- 仍生成 `api`、`domain`、`application`、`infra`、`trigger`、`starter` 六个模块。
- 仍拒绝非空目标目录，不提供覆盖选项。
- 仍保留退出码 `0、2、3、4、5、6` 的既有含义。
- 仍在静态验证后执行 Maven Wrapper `verify`，失败时保留生成目录和日志。
- 本次不增加上下文名、数据库类型、RPC 类型或依赖版本等交互参数。

## 5. 模块架构

### 5.1 依赖方向

```text
api                  独立公共契约
domain               独立领域模型
application  -> domain
infra        -> application + domain
trigger      -> api + application
starter      -> trigger + infra + application + domain
```

`api` 不依赖其他内部模块。`domain` 不依赖 Spring、Dubbo、MyBatis 或其他内部模块。`application` 不引用 API DTO、Infra 或 Trigger。`trigger` 不引用 Infra。Starter 是组合根。

### 5.2 包结构

以 `com.example.order` 为示例 `basePackage`，生成结构遵循：

```text
api
├─ com.example.order.api.SampleOrderApi
├─ com.example.order.facade.SampleOrderFacade
└─ com.example.order.model.sampleorder
   ├─ request
   └─ response

domain
└─ com.example.order.domain.sampleorder
   ├─ model
   └─ repository

application
└─ com.example.order.application.sampleorder
   ├─ command
   ├─ query
   ├─ model
   ├─ port
   ├─ mapping
   └─ service
      └─ impl

infra
└─ com.example.order.infra.sampleorder
   ├─ adapter
   ├─ mapping
   └─ persistence
      ├─ entity
      └─ mapper

trigger
└─ com.example.order.trigger
   ├─ http.sampleorder
   ├─ rpc.sampleorder
   └─ exception

starter
├─ com.example.order.OrderServiceApplication
└─ com.example.order.bean.SampleOrderBeanConfiguration
```

启动类继续由 `__DDD_PROJECT_CLASS__Application` 占位符派生；例如项目名为
`order-service` 时生成 `OrderServiceApplication`。启动类不以示例上下文命名。

公共 API 采用参考项目的稳定契约布局：HTTP 契约放 `api`，RPC 契约放 `facade`，公共传输对象放 `model.<context>.request|response`。内部模块统一使用 `<layer>.<context>.<role>`。

## 6. 命名与职责规则

| 类型 | 命名 | 位置 | 职责 |
| --- | --- | --- | --- |
| HTTP 契约 | `SampleOrderApi` | `api` | 声明 HTTP 操作与参数契约 |
| RPC 契约 | `SampleOrderFacade` | `facade` | 声明框架中立的 RPC Java 接口 |
| API 输入 | `*Request` | `model.sampleorder.request` | 公共传输输入和 Bean Validation 约束 |
| API 输出 | `*Response` | `model.sampleorder.response` | 公共传输输出 |
| 写输入 | `*Command` | `application.sampleorder.command` | Application 写用例输入 |
| 查询输入 | `*Criteria` | `application.sampleorder.query` | Application 查询条件 |
| 查询输出 | `*View` | `application.sampleorder.model` | Application 读模型 |
| 出站读取端口 | `*QueryPort` | `application.sampleorder.port` | Application 所需的最小读取能力 |
| 应用服务接口 | `*AppService`、`*QueryService` | `application.sampleorder.service` | 声明写用例和读用例 |
| 应用服务实现 | `*Impl` | `application.sampleorder.service.impl` | 编排事务、Domain 与端口 |
| 领域模型 | 业务名，如 `SampleOrder` | `domain.sampleorder.model` | 保护状态和业务不变式 |
| 写仓储端口 | `*Repository` | `domain.sampleorder.repository` | 加载和保存写模型 |
| 持久化对象 | `*DO` | `infra.sampleorder.persistence.entity` | 映射单张数据库表 |
| MyBatis 接口 | `*Mapper` | `infra.sampleorder.persistence.mapper` | 执行持久化 SQL |
| 持久化适配器 | `*PersistenceAdapter` | `infra.sampleorder.adapter` | 实现 Repository 与 Query Port |
| MapStruct 转换器 | `*Converter` | 每个边界自己的 `mapping` 或入口包 | 只转换相邻边界的类型 |
| HTTP 入口 | `*Controller` | `trigger.http.sampleorder` | 参数校验、协议转换和响应包装 |
| RPC 入口 | `*Provider` | `trigger.rpc.sampleorder` | 实现 Facade 并发布 Dubbo 服务 |
| 组合配置 | `*BeanConfiguration` | `starter` 的 `bean` | 显式创建 Application 与 Adapter Bean |

聚合根直接使用业务名 `SampleOrder`，不使用 `SampleOrderAggregate`。Mapper 专指 MyBatis 数据访问接口；Converter 专指对象转换器，避免角色混淆。转换器不得同时认识 API、Application、Domain 与 DO 四层类型。

## 7. SampleOrder 完整纵向示例

### 7.1 行为范围

示例支持：

- 创建订单；
- 按 ID 查询订单；
- 分页查询订单；
- 确认订单；
- 取消订单。

该范围足以展示 Command/Query 分离、领域状态转换、事务、HTTP/RPC 双入口和数据库适配，不增加支付、库存、事件或跨服务协作。

### 7.2 API 类型

- `SampleOrderApi`
- `SampleOrderFacade`
- `CreateSampleOrderRequest`
- `SampleOrderPageRequest`
- `SampleOrderResponse`
- `ApiResponse<T>`
- `PageResponse<T>`

HTTP Request 使用 Jakarta Bean Validation。Facade 保持普通 Java 接口，不携带 Dubbo 实现注解；发布注解只出现在 Trigger Provider。

### 7.3 Application 类型

- `CreateSampleOrderCommand`
- `ConfirmSampleOrderCommand`
- `CancelSampleOrderCommand`
- `SampleOrderCriteria`
- `SampleOrderPageCriteria`
- `SampleOrderView`
- `SampleOrderQueryPort`
- `SampleOrderApplicationConverter`
- `SampleOrderAppService` / `SampleOrderAppServiceImpl`
- `SampleOrderQueryService` / `SampleOrderQueryServiceImpl`

写服务负责创建、确认和取消；查询服务负责单条与分页读取。事务标注位于实现类。查询返回 View，不为读取重建 Domain Model。

### 7.4 Domain 类型与规则

- `SampleOrder`
- `SampleOrderId`
- `SampleOrderStatus`
- `SampleOrderRepository`
- `SampleOrderNotFoundException`
- `InvalidSampleOrderStateException`

`SampleOrder` 至少保护以下规则：新订单初始为待确认；只有待确认订单能够确认；只有待确认订单能够取消；终态订单不能再次转换。领域类使用 Lombok 支持的普通 class，不声明 Java record。

### 7.5 Infra 类型

- `SampleOrderDO`
- `SampleOrderMapper`
- `SampleOrderMapper.xml`
- `SampleOrderPersistenceConverter`
- `SampleOrderPersistenceAdapter`

`SampleOrderDO` 只描述示例订单表。Mapper XML 承担 SQL。Adapter 实现 Domain Repository 和 Application Query Port，并在写 SQL 前统一设置创建与更新时间。正式配置使用 MySQL 占位连接；测试使用 H2 的 MySQL 兼容模式与测试 Schema。

### 7.6 Trigger 与 Starter 类型

- `SampleOrderController`
- `SampleOrderHttpConverter`
- `SampleOrderProvider`
- `SampleOrderRpcConverter`
- `GlobalExceptionHandler`
- `SampleOrderBeanConfiguration`

Controller 实现 `SampleOrderApi`；Provider 实现 `SampleOrderFacade` 并使用 Apache Dubbo 发布。两者只依赖 Application Service。HTTP 通过统一异常处理器映射业务错误，RPC Provider 将业务错误转换为 `ApiResponse`，不暴露数据库或框架异常。

Application 和 Infra 核心类不使用 `@Service` 或 `@Repository` 自注册。`SampleOrderBeanConfiguration` 显式装配 Application Service、Persistence Adapter 和端口实现，Starter 不包含业务判断。

## 8. 数据流与边界转换

```text
HTTP Request
  -> SampleOrderController
  -> SampleOrderHttpConverter
  -> Command / Criteria
  -> AppService / QueryService
  -> Repository / QueryPort
  -> SampleOrderPersistenceAdapter
  -> SampleOrderPersistenceConverter
  -> SampleOrderMapper / XML

RPC Request
  -> SampleOrderProvider
  -> SampleOrderRpcConverter
  -> Command / Criteria
  -> 与 HTTP 共用同一 Application 调用链
```

API Request 不进入 Application；Application Command、Criteria 和 View 不进入 Domain；Domain Model 不作为 Response 或 DO；DO 不离开 Infra。HTTP 与 RPC 可以复用 Application 能力，但各自维护入口转换器和协议错误映射。

## 9. 错误处理

- Bean Validation 错误在 HTTP Trigger 边界转换为明确的 `ApiResponse` 失败结果。
- 找不到订单时，Application 抛出 `SampleOrderNotFoundException`。
- 非法状态转换由 Domain 抛出 `InvalidSampleOrderStateException`。
- HTTP 使用 `GlobalExceptionHandler` 映射预期业务异常。
- RPC Provider 捕获预期业务异常并转换为失败响应。
- 未预期基础设施异常不伪装成业务成功，测试也不得吞掉该类异常。

## 10. 技术基线

- Java 21
- Spring Boot 3.3.5
- Maven Wrapper 3.9.9
- MapStruct 1.6.3
- Lombok
- MyBatis Spring Boot Starter 3.0.4
- MySQL Connector/J
- H2 2.2.224，仅限测试
- Apache Dubbo 3.3.0
- JUnit 5、Mockito、Spring MVC Test

依赖版本集中在父 POM properties 或 dependency management 中，子模块不重复散落版本。重构保持当前模板已经验证的 Java、Spring Boot、Maven Wrapper 与 MapStruct 基线。Dubbo 使用官方 Spring Boot Starter，并固定为支持 JDK 21 与 Spring Boot 3 系列的 3.3.0；MyBatis Starter 固定为参考项目已使用的 3.0.4。若构建验证揭示依赖冲突，必须回到设计评审，不得在实施阶段静默换版。

## 11. Manifest 与静态验证

`template-manifest.json` 的 `template_version` 升级为 `2.0.0`。现有五个占位符保持不变。

Manifest 与验证器共同检查：

1. 六模块与固定模块顺序存在。
2. 内部 Maven 依赖符合第 5.1 节方向。
3. 必需示例路径和关键内容存在。
4. Java 包声明与源码目录一致。
5. Domain 不含 Spring、Dubbo、MyBatis 或内部模块导入。
6. Application 不引用 API、Infra 或 Trigger。
7. Trigger 不引用 Infra。
8. 角色后缀位于第 6 节指定包中。
9. `*DO` 只位于 `infra.*.persistence.entity`。
10. Java 源码不声明 record。
11. MapStruct Converter 使用静态 `INSTANCE`，并设置未映射字段策略为 ERROR。
12. 生成树中没有 `__DDD_*__` 或其他未知占位符残留。
13. Starter 正式 YAML 保留明显不可运行的 MySQL `xxxxx` 占位值。

静态验证针对受控模板和刚生成的工程，不尝试成为任意 Java 仓库的通用架构分析器。

## 12. 测试策略

### 12.1 Python 契约测试

- 保留现有输入、路径、复制、替换、平台命令和失败日志测试。
- 增加模块图、包路径、角色后缀、禁止导入和 MapStruct 规则测试。
- 验证 Manifest 2.0 的新字段和所有必需路径。
- 验证非默认 `groupId` 与 `basePackage` 的完整替换。

### 12.2 Java 模板测试

- Domain：初始状态、确认、取消和非法重复转换。
- Application：创建编排、加载后状态转换、Not Found、单条查询和分页查询。
- Infra：H2 Schema、Mapper XML、DO/Domain/View 转换及 Adapter 契约。
- Trigger：HTTP 参数校验、Controller 转换、异常映射、Provider 成功与业务失败映射。
- Starter：显式 Bean 装配、Spring Context 和模块架构守卫。

### 12.3 端到端验收

1. 从仓库根目录运行 `python -m unittest discover -s ddd-project-create/tests -v`。
2. 在原始模板运行 Windows `mvnw.cmd verify` 或 POSIX `./mvnw verify`。
3. 使用非默认项目名、groupId 和 basePackage 生成一次真实工程。
4. 确认生成树没有占位符、禁止依赖或生成器成功日志残留。
5. 在生成工程再次运行 Maven Wrapper `verify`。

只有全部步骤通过才能声明重构完成。

## 13. 文档更新

生成项目 README 增加“类型—包位置—职责”对照表、模块依赖图、HTTP/RPC 调用链、验证命令和删除示例说明。`references/template-contract.md` 同步记录 Manifest 2.0、命名检查和维护验收要求。`SKILL.md` 保持面向创建流程的简洁说明，不复制完整架构手册。

README 必须明确：

- `sampleorder` 是可删除示例，不是共享上下文。
- API DTO、Application 类型、Domain Model 和 DO 不得混用。
- Repository 表达写模型端口，Query Port 表达读取需求。
- 新业务应先完成领域建模，再按相同角色结构替换示例。

## 14. 实施范围

本次将修改：

- `ddd-project-create/assets/project-template` 下的父 POM、六个模块及完整示例代码；
- `ddd-project-create/assets/template-manifest.json`；
- `ddd-project-create/scripts/validate_project.py`，必要时小幅调整 `create_project.py` 以消费 Manifest 2.0；
- `ddd-project-create/tests/test_project_tools.py`；
- `ddd-project-create/references/template-contract.md`；
- `ddd-project-create/SKILL.md` 中与模板能力直接相关的说明。

生成器 CLI、安全边界和退出码不做破坏性变化。实施遵循测试先行，每一组规范先由失败测试表达，再修改模板或验证器使其通过。

## 15. 完成标准

- 生成器外部调用方式与安全行为兼容。
- Manifest 版本为 `2.0.0`，新架构与命名约束均有自动验证。
- 完整 SampleOrder HTTP/RPC、Command/Query、Domain、MyBatis 和 Starter 调用链可编译。
- 所有类型位于本设计规定的包中，类名角色明确。
- Domain、Application、Trigger 的禁止依赖检查通过。
- 原始模板与非默认参数生成工程的 Maven `verify` 均通过。
- Python 契约测试全部通过。
- 模板和文档不包含知识中心业务、团队私有依赖、仓库地址或环境配置。
