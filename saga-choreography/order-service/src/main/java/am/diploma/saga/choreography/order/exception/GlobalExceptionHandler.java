package am.diploma.saga.choreography.order.exception;

import am.diploma.saga.choreography.order.dto.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
