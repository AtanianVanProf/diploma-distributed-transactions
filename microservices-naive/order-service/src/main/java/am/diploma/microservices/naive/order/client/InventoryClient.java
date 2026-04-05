package am.diploma.microservices.naive.order.client;

import am.diploma.microservices.naive.order.dto.ErrorResponse;
import am.diploma.microservices.naive.order.dto.OrderItemRequest;
import am.diploma.microservices.naive.order.dto.ReserveStockRequest;
import am.diploma.microservices.naive.order.dto.ReserveStockResponse;
import am.diploma.microservices.naive.order.dto.StockItemRequest;
import am.diploma.microservices.naive.order.exception.ServiceCallException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryClient {

    @Qualifier("inventoryRestClient")
    private final RestClient inventoryRestClient;

    private final ObjectMapper objectMapper;

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

    private ErrorResponse parseErrorResponse(java.io.InputStream body) {
        try {
            return objectMapper.readValue(body, ErrorResponse.class);
        } catch (IOException e) {
            return null;
        }
    }
}
