package com.fiap.techchallenge.external.api.exception;

import com.fiap.techchallenge.domain.exception.DomainException;
import com.fiap.techchallenge.domain.exception.InvalidCpfException;
import com.fiap.techchallenge.domain.exception.InvalidEmailException;
import com.fiap.techchallenge.domain.exception.ProductLinkedToOrderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Object> handleDomainException(DomainException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCpfException.class)
    public ResponseEntity<Object> handleInvalidCpfException(InvalidCpfException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<Object> handleInvalidEmailException(InvalidEmailException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductLinkedToOrderException.class)
    public ResponseEntity<Object> handleProductLinkedToOrderException(ProductLinkedToOrderException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    private ResponseEntity<Object> buildResponse(String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", message);
        return new ResponseEntity<>(body, status);
    }
}
