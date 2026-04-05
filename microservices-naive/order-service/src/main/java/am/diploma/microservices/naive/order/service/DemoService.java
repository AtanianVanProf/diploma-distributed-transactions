package am.diploma.microservices.naive.order.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class DemoService {

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
    public void resetData() {
        // 1. Reset local order_db
        entityManager.createNativeQuery("TRUNCATE TABLE order_item, orders CASCADE").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE orders_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE order_item_id_seq RESTART WITH 1").executeUpdate();

        // 2. Fan out reset to inventory and payment services
        inventoryRestClient.post()
                .uri("/api/demo/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();

        paymentRestClient.post()
                .uri("/api/demo/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();

        log.info("All databases reset to initial state");
    }
}
