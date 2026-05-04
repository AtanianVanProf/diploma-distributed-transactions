package am.diploma.pvip.protocol.commitgate.service;

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
        entityManager.createNativeQuery("DELETE FROM participant_response").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM transaction_registry").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE transaction_registry_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE participant_response_id_seq RESTART WITH 1").executeUpdate();
    }
}
