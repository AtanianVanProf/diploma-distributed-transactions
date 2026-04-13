---
name: Kafka TYPE_MAPPINGS cross-service reference
description: Complete TYPE_MAPPINGS keys and event field structures for all saga-choreography services
type: reference
---

## Topics and producers

| Topic             | Producer Service   | TYPE_MAPPING keys                                          |
|-------------------|--------------------|-----------------------------------------------------------|
| order-events      | order-service      | (not yet implemented)                                     |
| inventory-events  | inventory-service  | stockReserved, stockReservationFailed, orderCompleted, orderFailed |
| payment-events    | payment-service    | paymentCharged, paymentFailed                             |

## Consumer subscriptions

| Consumer Service   | Topic              | Mapped keys                                    |
|--------------------|--------------------|-------------------------------------------------|
| inventory-service  | order-events       | orderPlaced                                     |
| inventory-service  | payment-events     | paymentCharged, paymentFailed                   |
| payment-service    | inventory-events   | stockReserved (+ default handler for others)    |

## Event field structures (must match between producer and consumer copies)

- **StockReservedEvent**: sagaId(Long), orderId(Long), customerId(Long), items(List<ReservedItem>), totalAmount(BigDecimal)
  - **ReservedItem**: productId(Long), productName(String), quantity(Integer), price(BigDecimal)
- **PaymentChargedEvent**: sagaId(Long), orderId(Long), customerId(Long), amount(BigDecimal)
- **PaymentFailedEvent**: sagaId(Long), orderId(Long), reason(String)

**How to apply:** When reviewing new services, verify their TYPE_MAPPINGS keys match this table and event field structures are identical.
