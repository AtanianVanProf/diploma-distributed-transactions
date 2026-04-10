package am.diploma.saga.orchestrator.orchestrator.client;

import am.diploma.saga.orchestrator.orchestrator.dto.ErrorResponse;
import am.diploma.saga.orchestrator.orchestrator.dto.OrderItemRequest;
import am.diploma.saga.orchestrator.orchestrator.dto.inventory.ReleaseStockRequest;
import am.diploma.saga.orchestrator.orchestrator.dto.inventory.ReleaseStockResponse;
import am.diploma.saga.orchestrator.orchestrator.dto.inventory.ReserveStockRequest;
import am.diploma.saga.orchestrator.orchestrator.dto.inventory.ReserveStockResponse;
import am.diploma.saga.orchestrator.orchestrator.dto.inventory.StockItemRequest;
import am.diploma.saga.orchestrator.orchestrator.exception.ServiceCallException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;

@Component
public class InventoryClient {

    private final RestClient inventoryRestClient;
    private final ObjectMapper objectMapper;

    public InventoryClient(@Qualifier("inventoryRestClient") RestClient inventoryRestClient,
                           ObjectMapper objectMapper) {
        this.inventoryRestClient = inventoryRestClient;
        this.objectMapper = objectMapper;
    }

    public ReserveStockResponse reserveStock(List<OrderItemRequest> items) {
        List<StockItemRequest> stockItems = items.stream()
                .map(item -> new StockItemRequest(item.getProductId(), item.getQuantity()))
                .toList();

        ReserveStockRequest request = new ReserveStockRequest(stockItems);

        try {
            return inventoryRestClient.post()
                    .uri("/api/products/reserve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                        ErrorResponse errorResponse = parseErrorResponse(response.getBody());
                        throw new ServiceCallException(
                                "INVENTORY",
                                errorResponse != null ? errorResponse.getError() : "UNKNOWN_ERROR",
                                errorResponse != null ? errorResponse.getMessage() : "Unknown error from inventory service"
                        );
                    })
                    .body(ReserveStockResponse.class);
        } catch (ServiceCallException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            throw new ServiceCallException(
                    "INVENTORY",
                    "INVENTORY_SERVICE_UNAVAILABLE",
                    "Inventory service is unavailable: " + ex.getMessage()
            );
        }
    }

    public ReleaseStockResponse releaseStock(List<StockItemRequest> items) {
        ReleaseStockRequest request = new ReleaseStockRequest(items);

        try {
            return inventoryRestClient.post()
                    .uri("/api/products/release")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                        ErrorResponse errorResponse = parseErrorResponse(response.getBody());
                        throw new ServiceCallException(
                                "INVENTORY",
                                errorResponse != null ? errorResponse.getError() : "UNKNOWN_ERROR",
                                errorResponse != null ? errorResponse.getMessage() : "Unknown error from inventory service"
                        );
                    })
                    .body(ReleaseStockResponse.class);
        } catch (ServiceCallException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceCallException(
                    "INVENTORY",
                    "INVENTORY_COMPENSATION_FAILED",
                    "Failed to release stock: " + ex.getMessage()
            );
        }
    }

    private ErrorResponse parseErrorResponse(java.io.InputStream body) {
        try {
            return objectMapper.readValue(body, ErrorResponse.class);
        } catch (IOException e) {
            return null;
        }
    }
}
