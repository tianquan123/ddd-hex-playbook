# Java Spring Hexagonal Rubric

Score each case from 0 to 6.

1. States whether the recommendation is an invariant, team default, conditional option, or example.
2. Honors user and project decisions before playbook defaults.
3. Assigns responsibility to the actual consumer and preserves dependency direction.
4. Does not force six modules, a global Convertor, cross-aggregate transactions, or cache-in-Repository.
5. Distinguishes DomainEvent, IntegrationEvent, Spring transaction events, and reliable Outbox delivery when relevant.
6. Provides a concrete verification method such as an architecture test, integration test, or failure scenario.

Passing score: every case scores at least 5/6 and the suite average is at least 5.5/6.
