# Java Spring Hexagonal Evaluation Cases

## J1 – Small service modules
This Spring Boot service has six endpoints and one aggregate. Which Maven modules must we create?

## J2 – Outbound port ownership
Only `PlaceOrderAppService` calls the payment provider. Should `PaymentClient` live in Domain or Application?

## J3 – Boundary conversion
Where should conversions between HTTP request, application input, aggregate, database row, and HTTP response live?

## J4 – Reliable event delivery
After saving an order we must reliably notify another service through MQ. Show the transaction and event flow.

## J5 – Query and cache
We need order pagination and Redis caching. Should these methods and cache logic go into `OrderRepository`?

## J6 – Trigger idempotency
An MQ listener needs duplicate-message protection and the same use case is also called over HTTP. Where does idempotency belong?

## J7 – Existing ADR override
Our ADR requires a single Maven module and constructor-injected MapStruct mappers. The playbook examples use six modules and static `INSTANCE`. Which rule wins?
