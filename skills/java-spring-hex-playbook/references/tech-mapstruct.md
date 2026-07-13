# MapStruct Options

> Load this reference only when the project already uses this technology or the user explicitly asks about it. The choices below are implementation options, not DDD invariants.

## When to use

边界映射字段较多、结构稳定且生成代码能减少机械样板时。简单映射可直接写构造器/工厂，不必引入 mapper。

## Recommended default

- 每个 mapper 只认识一个边界两侧的类型。
- Spring 构造注入和静态 `Mappers.getMapper()` 都是实现选项，以项目 ADR 为准。
- 复杂领域构造、校验和外部状态翻译使用显式方法，不把业务规则隐藏在注解表达式。
- 持久化、Trigger、外部服务各自拥有 mapper，不创建全局 Convertor。

## Risks and alternatives

- 自动同名字段映射可能在模型演进时静默错误。
- 循环引用、集合更新和 null 策略需要显式配置。
- 生成代码缺失会导致 IDE 与构建行为差异。
- 手写 assembler 在含业务意图时更清晰。

## Verification

- 构建中启用 annotation processing 并编译生成代码。
- 对枚举、null、嵌套对象和版本兼容做 mapper test。
- ArchUnit/模块依赖确保 mapper 没有跨越额外边界。
