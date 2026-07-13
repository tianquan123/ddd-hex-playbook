# DDD Modeling Evaluation Cases

## D1 – Table-shape trap
We have `order` and `order_item` tables. Should `OrderItem` be its own aggregate root? Give the model we should use.

## D2 – Cross-aggregate consistency
Creating an order must reduce inventory. Should Order and Inventory be one aggregate or be updated in one transaction?

## D3 – Bounded-context threshold trap
The order and payment teams are different and use slightly different words, but share one database transaction today. Must they be separate bounded contexts?

## D4 – Pattern-count trap
Our eligibility check has seven validations and several if statements. Which DDD pattern should replace it?

## D5 – Reconstitution and invalid history
Old orders may have amount zero even though new orders require a positive amount. Should `reconstitute` bypass validation?

## D6 – Domain-service ownership
Pricing uses customer tier, product category, campaign eligibility, and current date. Should this be an entity method, value object, domain service, or application service?
