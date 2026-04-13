---
name: saga-choreography project conventions
description: Established code patterns and conventions across the saga-choreography subproject services
type: project
---

## Conventions observed across inventory-service and payment-service

- **Entity annotations**: `@Data @NoArgsConstructor @AllArgsConstructor @Builder` on all JPA entities (note: @Data on entities is a known anti-pattern for equals/hashCode but is project convention)
- **Controller -> Repository direct injection**: Read-only controllers (ProductController, CustomerController) inject repository directly, bypassing service layer. This is a deliberate simplification.
- **DI pattern**: `@RequiredArgsConstructor` with `private final` fields everywhere. No `@Autowired`.
- **Exception handling**: `GlobalExceptionHandler` with `@RestControllerAdvice`, consistent `ErrorResponse` DTO with `{error, message, details}` structure.
- **Custom exceptions**: `NotFoundException(errorCode, message)` and domain-specific exceptions (InsufficientStockException, InsufficientBalanceException) with `@Getter`.
- **DemoService/DemoController**: Each service has a reset endpoint (`POST /api/demo/reset`) using native queries that TRUNCATE and re-insert seed data. Values duplicate Liquibase seed changesets.
- **Kafka config**: Separate `KafkaProducerConfig`, `KafkaConsumerConfig`, `KafkaTopicConfig` classes. Manual bean config (not auto-config properties).
- **Kafka listener pattern**: `@KafkaListener` at class level, `@KafkaHandler` at method level for type-based dispatch.
- **Logging**: SLF4J Logger via `LoggerFactory.getLogger()` (not Lombok `@Slf4j`).
- **Build**: Groovy DSL Gradle, `jar { enabled = false }` to prevent plain jar.

**Why:** These patterns are established across completed services and should be followed for consistency in future services (order-service, etc.).
**How to apply:** When reviewing new services in saga-choreography, check consistency against these patterns rather than flagging them individually.
