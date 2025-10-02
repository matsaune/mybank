package no.experis.bgo.mybank.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 */
@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Map<String, String>> unsupportedOperationExceptionHandler(UnsupportedOperationException exception) {
        log.error("Caught exception handling request, type={}", exception.getClass(), exception);
        Map<String, String> error = new HashMap<>();
        error.put("message", "Operation not currently implemented");
        error.put("permanent", "false");

        return new ResponseEntity<>(error, HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> illegalArgumentExceptionHandler(IllegalArgumentException exception) {
        log.error("Caught exception handling request, type={}", exception.getClass(), exception);
        Map<String, String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        error.put("permanent", "false");

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException exception) {
        log.error("Caught exception handling request, type={}", exception.getClass(), exception);
        Map<String, String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        error.put("permanent", "false");

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        log.error("Caught exception handling request, type={}", exception.getClass(), exception);
        Map<String, String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        error.put("permanent", "false");

        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.error("Caught exception handling request, type={}", exception.getClass(), exception);
        Map<String, String> error = new HashMap<>();
        error.put("message", "A constraint violation occurred. This could be due to duplicate unique fields.");
        error.put("permanent", "true");

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        log.error("Caught exception handling request, type={}", exception.getClass(), exception);
        Map<String, String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        error.put("permanent", "false");

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }




    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> defaultExceptionHandler(Exception exception) {
        log.error("Caught exception handling request, type={}", exception.getClass(), exception);
        Map<String, String> error = new HashMap<>();
        error.put("message", "An unexpected error occurred");
        error.put("permanent", "false");

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
