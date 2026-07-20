# __DDD_PROJECT_NAME__

Team-standard Java 21 / Spring Boot six-module DDD project. The included `sampleorder` slice is a runnable architecture example, not a suggested product domain.

## Modules

- `__DDD_PROJECT_NAME__-api`: HTTP and Dubbo request/response contracts
- `__DDD_PROJECT_NAME__-domain`: framework-free domain model and repository ports
- `__DDD_PROJECT_NAME__-application`: commands, queries, use-case interfaces, and implementations
- `__DDD_PROJECT_NAME__-infra`: MyBatis persistence adapter, DO, mapper, and XML SQL
- `__DDD_PROJECT_NAME__-trigger`: HTTP controller, Dubbo provider, protocol conversion, and exception mapping
- `__DDD_PROJECT_NAME__-starter`: Spring Boot startup, mapper scan, and explicit Bean composition

Dependencies point inward: Application depends on Domain; Infra implements Domain/Application ports; Trigger calls Application and publishes API contracts; Starter assembles everything.

## Naming map

```text
api.SampleOrderApi                       facade.SampleOrderFacade
model.sampleorder.request/*Request       model.sampleorder.response/*Response
domain.sampleorder.model/SampleOrder     domain.sampleorder.repository/*Repository
application.sampleorder.service/*        application.sampleorder.service.impl/*Impl
infra.sampleorder.adapter/*PersistenceAdapter
infra.sampleorder.persistence.entity/*DO
infra.sampleorder.persistence.mapper/*Mapper
trigger.http.sampleorder/*Controller     trigger.rpc.sampleorder/*Provider
bean/*BeanConfiguration                  (Starter only)
```

Domain types use business names such as `SampleOrder`; do not add an `Aggregate` suffix. Application services and persistence adapters are plain Java classes. Only Starter creates them as Beans.

## Run

The default profile uses an in-memory H2 database and a registry-free Dubbo provider:

```powershell
.\mvnw.cmd verify
.\mvnw.cmd -pl __DDD_PROJECT_NAME__-starter -am spring-boot:run
```

On macOS/Linux use `./mvnw`. The HTTP sample is available under `/sample/orders`.

For a production database or registry, set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER`, `DUBBO_REGISTRY_ADDRESS`, and optionally `DUBBO_PROTOCOL_PORT`.

After modeling the real domain, replace the complete `sampleorder` slice consistently across all modules.
