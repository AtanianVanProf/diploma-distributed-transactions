package am.diploma.monolith.repository;

import am.diploma.monolith.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product JOIN FETCH o.customer")
    List<Order> findAllWithItems();

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product JOIN FETCH o.customer WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}
