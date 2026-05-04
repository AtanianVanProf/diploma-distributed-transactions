package am.diploma.pvip.protocol.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryValidationClient {

    private final RestClient inventoryRestClient;

    public InventoryValidationResponse validate(Long productId, Integer quantity) {
        log.info("Validating inventory: productId={}, quantity={}", productId, quantity);
        return inventoryRestClient.get()
                .uri("/api/inventory/validate?productId={productId}&quantity={quantity}", productId, quantity)
                .retrieve()
                .body(InventoryValidationResponse.class);
    }
}
