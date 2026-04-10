package am.diploma.saga.orchestrator.orchestrator.client;

import am.diploma.saga.orchestrator.orchestrator.dto.ErrorResponse;
import am.diploma.saga.orchestrator.orchestrator.dto.payment.ChargeRequest;
import am.diploma.saga.orchestrator.orchestrator.dto.payment.ChargeResponse;
import am.diploma.saga.orchestrator.orchestrator.dto.payment.RefundRequest;
import am.diploma.saga.orchestrator.orchestrator.dto.payment.RefundResponse;
import am.diploma.saga.orchestrator.orchestrator.exception.ServiceCallException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigDecimal;

@Component
public class PaymentClient {

    private final RestClient paymentRestClient;
    private final ObjectMapper objectMapper;

    public PaymentClient(@Qualifier("paymentRestClient") RestClient paymentRestClient,
                         ObjectMapper objectMapper) {
        this.paymentRestClient = paymentRestClient;
        this.objectMapper = objectMapper;
    }

    public ChargeResponse charge(Long customerId, BigDecimal amount) {
        ChargeRequest request = new ChargeRequest(customerId, amount);

        try {
            return paymentRestClient.post()
                    .uri("/api/payments/charge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                        ErrorResponse errorResponse = parseErrorResponse(response.getBody());
                        throw new ServiceCallException(
                                "PAYMENT",
                                errorResponse != null ? errorResponse.getError() : "UNKNOWN_ERROR",
                                errorResponse != null ? errorResponse.getMessage() : "Unknown error from payment service"
                        );
                    })
                    .body(ChargeResponse.class);
        } catch (ServiceCallException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            throw new ServiceCallException(
                    "PAYMENT",
                    "PAYMENT_SERVICE_UNAVAILABLE",
                    "Payment service is unavailable: " + ex.getMessage()
            );
        }
    }

    public RefundResponse refund(Long customerId, BigDecimal amount) {
        RefundRequest request = new RefundRequest(customerId, amount);

        try {
            return paymentRestClient.post()
                    .uri("/api/payments/refund")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                        ErrorResponse errorResponse = parseErrorResponse(response.getBody());
                        throw new ServiceCallException(
                                "PAYMENT",
                                errorResponse != null ? errorResponse.getError() : "UNKNOWN_ERROR",
                                errorResponse != null ? errorResponse.getMessage() : "Unknown error from payment service"
                        );
                    })
                    .body(RefundResponse.class);
        } catch (ServiceCallException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceCallException(
                    "PAYMENT",
                    "PAYMENT_COMPENSATION_FAILED",
                    "Failed to refund payment: " + ex.getMessage()
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
