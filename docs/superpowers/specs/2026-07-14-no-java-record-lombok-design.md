# 使用 Lombok 替代 Java Record

日期：2026-07-14

## 背景与目标

`ddd-project-create` 的六模块模板和已生成的 `example-test` 使用了 Java Record 表示 HTTP DTO、应用命令和领域标识。团队约定项目 Java 源码不使用 Record；HTTP 请求需要使用 Bean Validation，并由 Spring MVC 在进入应用层前完成格式校验。

本次变更同时覆盖技能约束、项目模板和现有 `example-test`。生成器对 Java 关键字的校验仍保留 `record`，因为它依然是非法包名片段；普通文档中的英文名词 record 不在禁用范围内。

## 设计

### 类型替换

- `CreateSampleOrderRequest`：使用 Lombok `@Getter`、`@Setter`、`@NoArgsConstructor`。`productCode` 标注 `@NotBlank`，`quantity` 标注 `@Positive`。
- `CreateSampleOrderResponse`：使用 Lombok `@Getter`、`@AllArgsConstructor`。
- `CreateSampleOrderCommand`：使用 Lombok `@Getter`、`@AllArgsConstructor`，字段保持 `final`。
- `SampleOrderId`：使用 Lombok `@Value`，由 Lombok 生成构造器、getter、`equals`、`hashCode` 和 `toString`。

调用方统一改用标准 getter，如 `request.getProductCode()`、`command.getQuantity()` 和 `id.getValue()`。不对其他普通类做无关的 Lombok 重构。

### 校验边界

HTTP Controller 使用 `@Valid @RequestBody`。字段格式约束位于 API 请求 DTO；Trigger 负责触发校验并把合法请求映射为 Application Command。业务资格与聚合不变式仍由 Application/Domain 负责。

依赖调整：

- API：Lombok、`jakarta.validation-api`。
- Application、Domain：Lombok。
- Trigger：`spring-boot-starter-validation`。

所有依赖版本继续由 Spring Boot 父 POM 管理，不新增手工版本号。

## 技能和模板约束

`ddd-project-create/SKILL.md` 与模板契约明确：生成的 Java 源码不得声明 Java Record。契约测试扫描模板中的 `.java` 文件，拒绝 `record TypeName(...)` 声明，但不误伤生成器关键字列表或自然语言中的 record。

已生成的 `example-test` 与模板保持相同实现。由于生成器禁止覆盖非空目录，本次直接同步必要源码和 POM 变更，不重新生成项目。

## 测试与验收

1. 先增加失败的模板契约测试，证明当前模板仍含 Java Record。
2. 增加 Trigger Web 测试：空 `productCode` 或非正数 `quantity` 返回 HTTP 400，且不进入应用服务。
3. 修改模板与 `example-test` 后运行 Python 契约测试。
4. 扫描模板和 `example-test` 的 Java 源码，确认不存在 Java Record 声明。
5. 在正常用户环境分别对原始模板和 `example-test` 执行 Maven `verify`。

验收标准：契约测试通过、参数校验测试通过、两个 Maven Reactor 构建成功、Java Record 声明扫描结果为零。
