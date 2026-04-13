package am.diploma.saga.choreography.order.repository;

import am.diploma.saga.choreography.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
