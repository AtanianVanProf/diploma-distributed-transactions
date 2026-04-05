package am.diploma.microservices.naive.inventory.service;

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
        entityManager.createNativeQuery("TRUNCATE TABLE product CASCADE").executeUpdate();

        entityManager.createNativeQuery(
                "INSERT INTO product (id, name, sku, price, stock) VALUES (1, 'Laptop Pro 15', 'LAP-PRO-15', 1299.99, 10)")
                .executeUpdate();
        entityManager.createNativeQuery(
                "INSERT INTO product (id, name, sku, price, stock) VALUES (2, 'Wireless Mouse', 'WRL-MOUSE', 29.99, 3)")
                .executeUpdate();
        entityManager.createNativeQuery(
                "INSERT INTO product (id, name, sku, price, stock) VALUES (3, 'USB-C Hub', 'USB-C-HUB', 49.99, 1)")
                .executeUpdate();
        entityManager.createNativeQuery(
                "INSERT INTO product (id, name, sku, price, stock) VALUES (4, 'Mechanical Keyboard', 'MECH-KB', 89.99, 0)")
                .executeUpdate();

        entityManager.createNativeQuery("ALTER SEQUENCE product_id_seq RESTART WITH 5").executeUpdate();
    }
}
