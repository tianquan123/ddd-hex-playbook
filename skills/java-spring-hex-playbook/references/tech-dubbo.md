# Dubbo Options

> Load this reference only when the project already uses this technology or the user explicitly asks about it. The choices below are implementation options, not DDD invariants.

## When to use

项目已有 Dubbo 服务治理、需要内部 RPC 契约并接受其部署与注册中心模型时。

## Recommended default

- Dubbo Provider 放 RPC Trigger；Reference/SDK 只存在于出站适配器。
- 对外契约独立版本化时使用可选 API 模块。
- 非幂等写默认不做透明重试，使用业务幂等键与未知结果查询。
- timeout 从端到端预算倒推，不复制统一毫秒数。
- 适配器把 Dubbo DTO、业务拒绝和技术异常映射到内部契约。

## Risks and alternatives

- `retries > 0` 可能重复非幂等写。
- `catch(Throwable)` 会掩盖编程错误。
- Fallback 不能为支付/扣款等写操作伪造成功。
- HTTP/gRPC/消息可能更适合跨语言、流式或异步场景。

## Verification

- Contract test 验证版本、序列化与错误兼容。
- 故障测试覆盖超时、重复请求、下游拒绝和未知结果。
- ArchUnit 禁止 Domain/Application import Dubbo 类型。

