# __DDD_PROJECT_NAME__

This project was created from the team's six-module Java/Spring DDD template.

The `sample` context is a removable architecture example. It demonstrates dependency direction and boundary translation; it is not a domain model for your product. Model the real business with `ddd-modeling`, then replace or remove every `SampleOrder` type.

## Modules

- `__DDD_PROJECT_NAME__-api`: published request and response contracts
- `__DDD_PROJECT_NAME__-domain`: framework-free domain model and ports
- `__DDD_PROJECT_NAME__-application`: use-case orchestration
- `__DDD_PROJECT_NAME__-infra`: outbound adapters
- `__DDD_PROJECT_NAME__-trigger`: inbound HTTP adapter
- `__DDD_PROJECT_NAME__-starter`: Spring Boot assembly

## Verify

Run `mvnw.cmd verify` on Windows or `./mvnw verify` on macOS and Linux.
