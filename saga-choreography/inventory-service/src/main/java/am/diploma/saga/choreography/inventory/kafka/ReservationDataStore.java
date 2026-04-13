package am.diploma.saga.choreography.inventory.kafka;

import am.diploma.saga.choreography.inventory.event.StockReservedEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReservationDataStore {

    public record ReservationData(
            Long customerId,
            List<StockReservedEvent.ReservedItem> items,
            BigDecimal totalAmount
    ) {}

    private final ConcurrentHashMap<Long, ReservationData> store = new ConcurrentHashMap<>();

    public void save(Long sagaId, ReservationData data) {
        store.put(sagaId, data);
    }

    public ReservationData retrieve(Long sagaId) {
        return store.remove(sagaId);
    }

    public void clear() {
        store.clear();
    }
}
