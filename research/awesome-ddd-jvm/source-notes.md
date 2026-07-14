# Source Notes

Accessed: 2026-07-14

Evidence labels:

- `stated`: explicitly documented by the project or source author.
- `observed`: directly visible in source, tests, or repository structure.
- `inference`: this report's interpretation from cited evidence.

## Core projects

### ddd-by-examples/library

- URL: https://github.com/ddd-by-examples/library
- Business: `stated` A public-library domain with circulating/restricted books, researcher/regular patrons, holds, checkouts, overdue rules, and daily sheets. The README ties these rules to Big Picture EventStorming, Example Mapping, and Design Level EventStorming.
- Architecture: `stated` Modular monolith by bounded-context package. Lending uses a hexagonal domain model; the simpler catalogue context uses CRUD. Read models such as Patron Profile and Daily Sheets are separated where Event Storming exposed different query needs.
- Consistency: `stated` The same behavioral test is demonstrated with immediate and eventual event delivery. `observed` `StoreAndForwardDomainEventPublisher` stores events and publishes them periodically; eventual-consistency tests use Spock `PollingConditions`.
- Tests: `observed` Business-rule Spock specifications, integration tests for strong/eventual consistency, optimistic locking tests, and ArchUnit rules for module, hexagon, and Spring dependency boundaries.
- Code: [AvailableBook](https://github.com/ddd-by-examples/library/blob/master/src/main/java/io/pillopl/library/lending/book/model/AvailableBook.java), [store-and-forward publisher](https://github.com/ddd-by-examples/library/blob/master/src/main/java/io/pillopl/library/commons/events/publisher/StoreAndForwardDomainEventPublisher.java), [hexagonal ArchUnit rules](https://github.com/ddd-by-examples/library/blob/master/src/test/groovy/io/pillopl/library/lending/architecture/LendingHexagonalArchitectureTest.java), [eventual consistency test](https://github.com/ddd-by-examples/library/blob/master/src/integration-test/groovy/io/pillopl/library/lending/eventspropagation/EventualConsistencyBetweenAggregatesAndReadModelsIT.groovy).
- Caveat: Latest checked commit is 2022-05-25. It intentionally explores several styles (functional Java/Vavr, no ORM, event-based state) and should be mined for decisions and tests rather than copied as a team template.

### citerus/dddsample-core

- URL: https://github.com/citerus/dddsample-core
- Business: `stated` The canonical cargo booking/routing/tracking domain created with Domain Language and Citerus. Cargo, Route Specification, Itinerary, Leg, Delivery, Handling Event, Location, and Voyage form a cohesive shipping language.
- Architecture: `observed` Domain model, application services, interfaces, and infrastructure are separated. `RoutingService` is a domain-facing abstraction; the application service owns Spring transactions; adapters convert web/file input and implement persistence/routing.
- Consistency: `observed` Routing changes inside Cargo update Delivery synchronously. Handling history belongs to a different aggregate and updates cargo delivery through application events.
- Tests: `observed` Aggregate/value-object tests, application service tests, JPA repository tests, file/REST integration tests, acceptance tests, and a full Cargo lifecycle scenario.
- Code: [Cargo aggregate](https://github.com/citerus/dddsample-core/blob/master/src/main/java/se/citerus/dddsample/domain/model/cargo/Cargo.java), [BookingServiceImpl](https://github.com/citerus/dddsample-core/blob/master/src/main/java/se/citerus/dddsample/application/impl/BookingServiceImpl.java), [Cargo lifecycle scenario](https://github.com/citerus/dddsample-core/blob/master/src/test/java/se/citerus/dddsample/scenario/CargoLifecycleScenarioTest.java).
- Caveat: The project is a pedagogical reference with historical roots, though it is still maintained (latest checked commit 2025-06-02). Its package names and JPA choices are examples, not DDD invariants.

### eclipse-ee4j/cargotracker

- URL: https://github.com/eclipse-ee4j/cargotracker
- Business: `stated` An end-to-end Jakarta EE adaptation of the canonical cargo domain, covering booking, routing, handling, tracking, and administrative interfaces.
- Architecture: `observed` Similar domain/application/interface/infrastructure boundaries to DDDSample, with Jakarta EE/JPA annotations and external routing as an adapter. This makes it useful for separating durable model ideas from platform-specific implementation.
- Consistency: `observed` Cargo documentation explicitly distinguishes synchronous updates for RouteSpecification/Itinerary inside the aggregate from asynchronous Delivery derivation after HandlingEvent changes in another aggregate.
- Tests: `observed` Domain tests, application tests, infrastructure tests, and a full lifecycle scenario using real application components with in-memory infrastructure substitutes.
- Code: [Cargo aggregate](https://github.com/eclipse-ee4j/cargotracker/blob/master/src/main/java/org/eclipse/cargotracker/domain/model/cargo/Cargo.java), [Cargo lifecycle scenario](https://github.com/eclipse-ee4j/cargotracker/blob/master/src/test/java/org/eclipse/cargotracker/scenario/CargoLifecycleScenarioTest.java), [project site](https://eclipse-ee4j.github.io/cargotracker/).
- Caveat: Platform annotations appear in domain entities, which conflicts with this repository's framework-free Domain invariant. Latest checked commit is 2025-11-12; code requires Java 11 or 17 according to the checked README.

### VaughnVernon/IDDD_Samples

- URL: https://github.com/VaughnVernon/IDDD_Samples
- Business: `stated` Reference bounded contexts for Identity & Access, Collaboration, and Agile Project Management from *Implementing Domain-Driven Design*.
- Architecture: `observed` Separate top-level projects make context boundaries explicit. Collaboration contains application services, port/adapters, event-store repositories, MySQL projections, and messaging listeners. Identity & Access uses ORM, illustrating that local implementation can differ per context.
- Consistency: `stated` Collaboration writes an event-sourced model and CQRS read model to different stores but in one thread to keep the example simple; the README explicitly admits a small inconsistency window.
- Tests: `observed` Domain and application service tests exist across contexts, but the README warns that some tests are incomplete.
- Code: [README caveats](https://github.com/VaughnVernon/IDDD_Samples/blob/master/README.md), [DiscussionApplicationService](https://github.com/VaughnVernon/IDDD_Samples/blob/master/iddd_collaboration/src/main/java/com/saasovation/collaboration/application/forum/DiscussionApplicationService.java), [MySQLProjectionDispatcher](https://github.com/VaughnVernon/IDDD_Samples/blob/master/iddd_collaboration/src/main/java/com/saasovation/collaboration/port/adapter/persistence/view/MySQLProjectionDispatcher.java).
- Caveat: The project explicitly says it is not production-quality; it requires Java 7 and states Java 8+ does not work. Latest checked commit is 2022-03-26. Use it for conceptual reference, not dependencies or build conventions.

### asc-lab/java-cqrs-intro

- URL: https://github.com/asc-lab/java-cqrs-intro
- Business: `stated` Insurance-policy examples used to explain CQRS and Event Sourcing as architectural choices rather than vendor frameworks.
- Architecture: `observed` Parallel modules show `nocqrs`, `separatemodels`, and `cqrswithes`. The README diagrams a progression from no CQRS, to separate commands/queries, separate models, separate stores, then Event Sourcing.
- Consistency: `observed` Separate-model code publishes policy events into projections; the ES variant rebuilds policy aggregates from event history and writes through an EventStore interface.
- Tests: `observed` Spock domain specifications and Java projection/finder tests make each stage comparable.
- Code: [evolutionary README](https://github.com/asc-lab/java-cqrs-intro/blob/master/README.md), [separate-model projection tests](https://github.com/asc-lab/java-cqrs-intro/tree/master/separatemodels/src/test/java/pl/altkom/asc/lab/cqrs/intro/separatemodels), [event-sourced AggregateRoot](https://github.com/asc-lab/java-cqrs-intro/blob/master/cqrswithes/src/main/java/pl/altkom/asc/lab/cqrs/intro/cqrswithes/domain/base/AggregateRoot.java).
- Caveat: Latest checked commit is 2020-07-20 and some stages shown in README are conceptual diagrams rather than independent modules. The value is the decision gradient, not current Spring style.

### andreschaffer/event-sourcing-cqrs-examples

- URL: https://github.com/andreschaffer/event-sourcing-cqrs-examples
- Business: `stated` A minimal bank domain: clients open accounts, deposit/withdraw money, and read transaction history/account summaries.
- Architecture: `stated` Ports and Adapters protect the write-side domain; read models use package-by-feature. Queries for a single aggregate may replay the event stream, while cross-aggregate queries use projections.
- Consistency: `stated` Event replay handlers only assign past state for idempotence. Event versions provide ordering, optimistic concurrency on writes, and out-of-order handling on reads. `observed` the in-memory EventStore rejects a stale base version.
- Tests: `observed` Aggregate rehydration, event store optimistic locking, projections, repositories, and specifications are directly tested.
- Code: [README design choices](https://github.com/andreschaffer/event-sourcing-cqrs-examples/blob/master/README.md), [Aggregate](https://github.com/andreschaffer/event-sourcing-cqrs-examples/blob/master/src/main/java/bankservice/domain/model/Aggregate.java), [InMemoryEventStore](https://github.com/andreschaffer/event-sourcing-cqrs-examples/blob/master/src/main/java/bankservice/port/outgoing/adapter/eventstore/InMemoryEventStore.java).
- Caveat: The code targets Java 14 and uses an in-memory event store; production concerns such as durable subscription checkpoints, dead letters, snapshots, schema evolution, and operational replay need separate design. Latest checked commit is 2026-07-13.

### mkopylec/project-manager

- URL: https://github.com/mkopylec/project-manager
- Business: `stated` A ten-step project-management workshop expressed in business language: teams, employees, project drafts, features, assignment, start/end policies, and reporting.
- Architecture: `stated` Each start/finish branch is a modeling step; dedicated branches compare classic layering, hexagonal architecture, unit of work, denormalized read model, and API organization.
- Consistency: `observed` The later requirements expose cross-object rules (team workload, feature completion) and an external reporting side effect, making it useful for discussing domain service, policy, event, and transaction boundaries.
- Tests: `stated` Participants implement requirements until supplied tests pass; the branch sequence makes model evolution reviewable.
- Code: [workshop README](https://github.com/mkopylec/project-manager/blob/master/README.md), [hexagonal solution branch](https://github.com/mkopylec/project-manager/tree/hexagonal_architecture), [class organization article](https://allegro.tech/2019/12/grouping-and-organizing-classes.html).
- Caveat: Master is workshop scaffolding and includes an unrelated `layers` example. The completed design lives on non-default branches. Latest checked master commit is 2020-10-30.

## Supporting workflow

### humank/ddd-practitioners-ref

- URL: https://github.com/humank/ddd-practitioners-ref
- Business: `stated` A coffee-shop/trip-service learning path that connects Wardley Mapping, Impact Mapping, EventStorming, Bounded Context Canvas, Aggregate Canvas, Example Mapping, Specification by Example, TDD, ports/adapters, and cloud deployment.
- Architecture: `observed` The repository separates domain, application, web, and infrastructure modules in the Java coffee-shop example, but its greater value is the discovery-to-code sequence.
- Consistency: `inference` Risky events and exception exploration occur before aggregate/context formation, which helps consistency decisions remain business-driven.
- Tests: `stated` Specification by Example produces test skeletons that are implemented with TDD.
- Code: [workflow outline](https://github.com/humank/ddd-practitioners-ref/blob/master/README.md), [modeling and development](https://github.com/humank/ddd-practitioners-ref/blob/master/docs/04-modeling-and-development/README.md), [coffee-shop sources](https://github.com/humank/ddd-practitioners-ref/tree/master/sources/coffeeshop).
- Caveat: Latest checked commit is 2022-11-09. The material is broad and unevenly edited; treat it as a workshop route and source index, not normative Java guidance.

## Community and articles

### heynickc/awesome-ddd

- URL: https://github.com/heynickc/awesome-ddd#sample-projects
- Supports: discovery index for DDD, CQRS, Event Sourcing, and EventStorming resources; JVM Sample Projects section supplied the candidate pool.
- Caveat: Descriptions are curated summaries, not verification of correctness, activity, or production fitness. Each selected project was checked at its original source.

### ddd-crew/context-mapping

- URL: https://github.com/ddd-crew/context-mapping
- Supports: context maps describe bounded-context and team contact through team relationships and integration patterns. The guide recommends small maps aimed at explicit questions rather than one universal landscape map.
- Evidence: [README](https://github.com/ddd-crew/context-mapping/blob/master/README.md), [cheat sheet assets](https://github.com/ddd-crew/context-mapping/tree/master/resources).
- Skill implication: `ddd-modeling` currently covers bounded-context discovery but lacks an explicit downstream step for upstream/downstream relationships, model propagation, governance, and ACL/Published Language/Open Host Service choices.

### Effective Aggregate Design

- URL: https://www.dddcommunity.org/library/vernon_2011/
- Supports: use business invariants and consistency boundaries to keep aggregates small, reference other aggregates by identity, and prefer eventual consistency across aggregate boundaries when the business allows it.
- Skill implication: reinforces existing aggregate guidance, but suggests adding scenario-based checks for cross-aggregate references and explicit consistency timing.

### DDD Heuristics

- URL: https://www.dddheuristics.com/
- Supports: a community catalogue of heuristics and discussion prompts rather than absolute rules.
- Skill implication: strengthens the repository's rule-level distinction: heuristics should be framed as conditional questions with counterexamples, not architecture invariants.

### Alberto Brandolini / EventStorming

- URL: https://ziobrando.blogspot.com/
- Supports: EventStorming is collaborative learning. Conflicting expert views may indicate different bounded contexts; unresolved questions should be marked as hotspots rather than silently resolved by the modeler.
- Evidence: [EventStorming site](https://www.eventstorming.com/), [Awesome DDD blog entry](https://github.com/heynickc/awesome-ddd#blogs).
- Skill implication: `ddd-modeling` should preserve actors, commands, events, policies, hotspots, and contradictory language as evidence before proposing aggregates.
