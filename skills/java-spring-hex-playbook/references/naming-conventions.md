# Naming Conventions

本文件全部是团队默认值，可被项目现有代码与 ADR 覆盖。命名用于表达职责，不用于证明领域类型或架构边界。

## Recommended defaults

| 职责 | 默认命名 |
|---|---|
| 应用用例 | `{Xxx}AppService` 或动词型 Use Case |
| 聚合仓储端口 | `{Xxx}Repository` |
| 默认关系库适配器 | `{Xxx}RepositoryImpl` |
| 外部服务适配器 | `{Protocol}{Xxx}Adapter/Client` |
| 持久化行对象 | `{Xxx}DO`（项目已采用时） |
| HTTP/MQ/RPC/Job 入口 | `{Xxx}Controller/Listener/Provider/Job` |

不强制所有聚合使用 `Aggregate` 后缀，也不强制所有实体使用 `Entity` 后缀。若裸领域名称更符合通用语言，优先裸名。

## Mapper names

按职责选择：

- `Mapper`：结构映射；
- `Assembler`：组合多个输入形成领域对象/用例输出；
- `Translator`：防腐层语义翻译；
- `Converter`：通用类型转换。

不使用一个全局 `Convertor` 连接所有层；`Convertor` 拼写若已是项目约定可继续，但它只是默认命名。

## External numeric codes

按限界上下文划分数字码段可以保留为对外契约默认值。内部领域失败通过显式映射连接 code，不在 Domain 手写同步数字。

## Storage defaults

- 表/列：项目采用关系库时使用 `snake_case`。
- 索引：`idx_{table}_{columns}`，唯一键 `uk_{table}_{columns}`。
- Redis key：`{business}:{context}:{purpose}:{id}`，并声明 TTL、基数与数据所有者。
- Mapper XML、DO 和 DAO 命名遵循项目已有 MyBatis 约定。

## Verification

- 搜索同一职责是否出现多个后缀或同名异义。
- 检查命名是否能由通用语言解释。
- ADR 覆盖默认值时，以 ADR 为准并更新示例。

