package am.diploma.saga.orchestrator.orchestrator.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class DemoService {

    private final EntityManager entityManager;
    private final RestClient inventoryRestClient;
    private final RestClient paymentRestClient;
    private final RestClient orderRestClient;

    public DemoService(EntityManager entityManager,
                       @Qualifier("inventoryRestClient") RestClient inventoryRestClient,
                       @Qualifier("paymentRestClient") RestClient paymentRestClient,
                       @Qualifier("orderRestClient") RestClient orderRestClient) {
        this.entityManager = entityManager;
        this.inventoryRestClient = inventoryRestClient;
        this.paymentRestClient = paymentRestClient;
        this.orderRestClient = orderRestClient;
    }

    /**
     * Resets all data across all services: truncates local saga tables
     * and fans out reset calls to order, inventory, and payment services.
     */
    public void resetAllData() {
        resetLocalData();

        resetService(orderRestClient, "order");
        resetService(inventoryRestClient, "inventory");
        resetService(paymentRestClient, "payment");

        log.info("All databases reset to initial state");
    }

    /**
     * Truncates local saga tables and resets sequences.
     */
    @Transactional
    public void resetLocalData() {
        entityManager.createNativeQuery("TRUNCATE TABLE saga_step CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE saga_execution CASCADE").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE saga_execution_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE saga_step_id_seq RESTART WITH 1").executeUpdate();
    }

    private void resetService(RestClient restClient, String serviceName) {
        try {
            restClient.post()
                    .uri("/api/demo/reset")
                    .retrieve()
                    .body(String.class);
            log.info("{} service reset successfully", serviceName);
        } catch (Exception e) {
            log.error("Failed to reset {} service: {}", serviceName, e.getMessage());
        }
    }
}
