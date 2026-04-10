package am.diploma.saga.orchestrator.orchestrator.client;

import am.diploma.saga.orchestrator.orchestrator.dto.ErrorResponse;
import am.diploma.saga.orchestrator.orchestrator.dto.order.CreateOrderRequest;
import am.diploma.saga.orchestrator.orchestrator.dto.order.OrderResponse;
import am.diploma.saga.orchestrator.orchestrator.exception.ServiceCallException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Component
public class OrderClient {

    private final RestClient orderRestClient;
    private final ObjectMapper objectMapper;

    public OrderClient(@Qualifier("orderRestClient") RestClient orderRestClient,
                       ObjectMapper objectMapper) {
        this.orderRestClient = orderRestClient;
        this.objectMapper = objectMapper;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        try {
            return orderRestClient.post()
                    .uri("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                        ErrorResponse errorResponse = parseErrorResponse(response.getBody());
                        throw new ServiceCallException(
                                "ORDER",
                                errorResponse != null ? errorResponse.getError() : "UNKNOWN_ERROR",
                                errorResponse != null ? errorResponse.getMessage() : "Unknown error from order service"
                        );
                    })
                    .body(OrderResponse.class);
        } catch (ServiceCallException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            throw new ServiceCallException(
                    "ORDER",
                    "ORDER_SERVICE_UNAVAILABLE",
                    "Order service is unavailable: " + ex.getMessage()
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
