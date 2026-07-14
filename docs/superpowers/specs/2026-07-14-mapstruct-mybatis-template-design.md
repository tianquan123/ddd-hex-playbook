# Static MapStruct and MyBatis XML Design

## Scope

Apply the same design to both the generated `example-test` project and the
`ddd-project-create` template. Update the skill documentation, template
contract, and generator contract tests so future generated projects preserve
the design.

## Trigger mapping

Add a Trigger-layer `SampleOrderHttpMapper` dedicated to converting
`CreateSampleOrderRequest` into `CreateSampleOrderCommand`.

- Declare it as a MapStruct `@Mapper`.
- Expose a static `INSTANCE` initialized by `Mappers.getMapper(...)`.
- Make `SampleOrderController` call `SampleOrderHttpMapper.INSTANCE.toCommand(request)`.
- Keep request validation on the Lombok-backed request class and `@Valid` on
  the controller parameter.
- Configure MapStruct annotation processing in the Trigger module.

The mapper belongs to Trigger because it translates the inbound HTTP contract
into an application command.

## MySQL persistence adapter

Replace `InMemorySampleOrderRepository` with the Infra implementation
`com.tianquan.infra.sample.adapter.SampleOrderRepository`. It implements the
domain port with the same simple name by using the domain interface's fully
qualified name in the declaration.

The persistence adapter consists of:

- `SampleOrderRepository`: domain repository adapter.
- `SampleOrderMapper`: MyBatis mapper interface.
- `SampleOrderPO`: database persistence object.
- `SampleOrderMapper.xml`: explicit SQL mapping.

The MyBatis XML provides `insert`/upsert and `selectById` operations with
explicit columns. The adapter maps between the aggregate and persistence
object. MyBatis and persistence object types remain confined to Infra.

## Aggregate rehydration

Add a controlled `rehydrate(...)` factory to `SampleOrderAggregate`. It accepts
the persisted identifier, product code, quantity, and status, applies the same
invariants as aggregate creation, and restores the persisted state without
exposing a public all-arguments constructor.

## Database contract

Use this table shape:

```sql
CREATE TABLE sample_order (
    id VARCHAR(64) PRIMARY KEY,
    product_code VARCHAR(128) NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(32) NOT NULL
);
```

`save` uses MySQL upsert semantics so the repository contract supports both
initial persistence and later aggregate updates.

## Runtime configuration

Place datasource and MyBatis configuration in the Starter module's
`application.yml`. Use `xxxxx` for the host, database name, username, and
password. Configure the MySQL driver, mapper XML location, and underscore to
camel-case mapping.

The Infra module owns the MyBatis Spring Boot starter and MySQL driver
dependencies. Mapper discovery uses the MyBatis `@Mapper` marker, while the
existing infrastructure configuration creates the domain repository adapter.

## Template and skill maintenance

Mirror all Java, XML, POM, and YAML changes into `ddd-project-create`.
Document that generated projects use static MapStruct at the HTTP boundary and
MyBatis XML for the sample repository. Extend template validation to require
the mapper, XML, datasource configuration, and absence of the in-memory
repository.

No generated Java source may declare a Java `record`; DTOs, commands,
persistence objects, and value objects continue to use ordinary Lombok-backed
classes where appropriate.

## Verification

- Run the Python generator contract suite.
- Compile/package the template and `example-test` without requiring tests to
  connect to the placeholder database.
- Generate a project with non-default coordinates and validate its static
  structure and compilation.
- Search generated/template Java sources for `record` declarations and stale
  `InMemorySampleOrderRepository` references.

Runtime database integration tests are outside scope because the requested
configuration deliberately contains non-working `xxxxx` placeholders.
