package am.diploma.saga.choreography.order.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
public class DemoService {

    private static final Logger log = LoggerFactory.getLogger(DemoService.class);

    private final EntityManager entityManager;
    private final RestClient inventoryRestClient;
    private final RestClient paymentRestClient;

    public DemoService(EntityManager entityManager,
                       @Qualifier("inventoryRestClient") RestClient inventoryRestClient,
                       @Qualifier("paymentRestClient") RestClient paymentRestClient) {
        this.entityManager = entityManager;
        this.inventoryRestClient = inventoryRestClient;
        this.paymentRestClient = paymentRestClient;
    }

    @Transactional
    public void resetAllData() {
        resetLocalData();

        try {
            inventoryRestClient.post().uri("/api/demo/reset").retrieve().toBodilessEntity();
            log.info("Inventory service reset successful");
        } catch (Exception e) {
            log.warn("Failed to reset inventory service: {}", e.getMessage());
        }

        try {
            paymentRestClient.post().uri("/api/demo/reset").retrieve().toBodilessEntity();
            log.info("Payment service reset successful");
        } catch (Exception e) {
            log.warn("Failed to reset payment service: {}", e.getMessage());
        }
    }

    @Transactional
    public void resetLocalData() {
        entityManager.createNativeQuery("TRUNCATE TABLE saga_step, saga_execution, order_item, orders CASCADE").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE orders_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE order_item_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE saga_execution_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE saga_step_id_seq RESTART WITH 1").executeUpdate();
    }
}
