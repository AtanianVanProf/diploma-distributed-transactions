package am.diploma.saga.orchestrator.order.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DemoService {

    private final EntityManager entityManager;

    @Transactional
    public void resetData() {
        entityManager.createNativeQuery("TRUNCATE TABLE order_item, orders CASCADE").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE orders_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE order_item_id_seq RESTART WITH 1").executeUpdate();
    }
}
