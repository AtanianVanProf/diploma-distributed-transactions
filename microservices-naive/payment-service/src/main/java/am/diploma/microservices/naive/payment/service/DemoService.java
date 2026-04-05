package am.diploma.microservices.naive.payment.service;

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
        entityManager.createNativeQuery("TRUNCATE TABLE customer CASCADE").executeUpdate();

        entityManager.createNativeQuery(
                "INSERT INTO customer (id, name, email, balance) VALUES (1, 'Alice Johnson', 'alice@example.com', 10000.00)")
                .executeUpdate();
        entityManager.createNativeQuery(
                "INSERT INTO customer (id, name, email, balance) VALUES (2, 'Bob Smith', 'bob@example.com', 50.00)")
                .executeUpdate();

        entityManager.createNativeQuery("ALTER SEQUENCE customer_id_seq RESTART WITH 3").executeUpdate();
    }
}
