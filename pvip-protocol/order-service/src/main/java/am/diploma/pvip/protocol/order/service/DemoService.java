package am.diploma.pvip.protocol.order.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class DemoService {

    private final EntityManager entityManager;
    private final String inventoryUrl;
    private final String paymentUrl;
    private final String commitgateUrl;
    private final RestClient restClient;

    public DemoService(EntityManager entityManager,
                       @Value("${services.inventory-url}") String inventoryUrl,
                       @Value("${services.payment-url}") String paymentUrl,
                       @Value("${services.commitgate-url}") String commitgateUrl) {
        this.entityManager = entityManager;
        this.inventoryUrl = inventoryUrl;
        this.paymentUrl = paymentUrl;
        this.commitgateUrl = commitgateUrl;
        this.restClient = RestClient.create();
    }

    @Transactional
    public void resetAll() {
        log.info("Resetting all services");

        entityManager.createNativeQuery("DELETE FROM transaction_intent").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM protocol_execution").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM order_item").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM orders").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE orders_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE order_item_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE protocol_execution_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE transaction_intent_id_seq RESTART WITH 1").executeUpdate();

        try {
            restClient.post().uri(inventoryUrl + "/api/demo/reset").retrieve().toBodilessEntity();
            log.info("Inventory service reset");
        } catch (Exception e) {
            log.warn("Failed to reset inventory service: {}", e.getMessage());
        }

        try {
            restClient.post().uri(paymentUrl + "/api/demo/reset").retrieve().toBodilessEntity();
            log.info("Payment service reset");
        } catch (Exception e) {
            log.warn("Failed to reset payment service: {}", e.getMessage());
        }

        try {
            restClient.post().uri(commitgateUrl + "/api/demo/reset").retrieve().toBodilessEntity();
            log.info("Commit gate service reset");
        } catch (Exception e) {
            log.warn("Failed to reset commit gate service: {}", e.getMessage());
        }
    }
}
