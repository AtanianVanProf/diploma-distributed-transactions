package am.diploma.saga.orchestrator.inventory.exception;

import am.diploma.saga.orchestrator.inventory.dto.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error("INSUFFICIENT_STOCK")
                .message(ex.getMessage())
                .details(Map.of(
                        "productId", ex.getProductId(),
                        "available", ex.getAvailable(),
                        "requested", ex.getRequested()))
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .details(null)
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error("CONSTRAINT_VIOLATION")
                .message("A database constraint was violated")
                .details(null)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
