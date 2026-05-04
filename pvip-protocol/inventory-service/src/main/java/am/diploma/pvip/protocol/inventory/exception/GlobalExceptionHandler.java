package am.diploma.pvip.protocol.inventory.exception;

import am.diploma.pvip.protocol.inventory.dto.ErrorResponse;
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
        ErrorResponse response = new ErrorResponse(
                "INSUFFICIENT_STOCK",
                ex.getMessage(),
                Map.of(
                        "productId", ex.getProductId(),
                        "productName", ex.getProductName(),
                        "available", ex.getAvailable(),
                        "requested", ex.getRequested()
                )
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        ErrorResponse response = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ErrorResponse response = new ErrorResponse(
                "DATA_INTEGRITY_VIOLATION",
                "A data integrity constraint was violated",
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
