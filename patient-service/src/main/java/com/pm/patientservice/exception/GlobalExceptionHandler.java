package com.pm.patientservice.exception;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(
      GlobalExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationException(
      MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult().getFieldErrors().forEach(
        error -> errors.put(error.getField(), error.getDefaultMessage()));

    return ResponseEntity.badRequest().body(errors);
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<Map<String, String>> handleEmailAlreadyExistsException(
      EmailAlreadyExistsException ex) {

    // The exception message contains the email (personal data): not logged
    log.warn("Request rejected: email address already exists");
    Map<String, String> errors = new HashMap<>();
    errors.put("message", "Email address already exists");
    return ResponseEntity.badRequest().body(errors);
  }

  @ExceptionHandler(PatientNotFoundException.class)
  public ResponseEntity<Map<String, String>> handlePatientNotFoundException(
      PatientNotFoundException ex) {
    log.warn("Patient not found {}", ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    errors.put("message", "Patient not found");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errors);
  }

  @ExceptionHandler(DateTimeParseException.class)
  public ResponseEntity<Map<String, String>> handleDateTimeParseException(
      DateTimeParseException ex) {
    log.warn("Invalid date {}", ex.getParsedString());

    Map<String, String> errors = new HashMap<>();
    errors.put("message",
        "Invalid date '" + ex.getParsedString() + "', expected format yyyy-MM-dd");
    return ResponseEntity.badRequest().body(errors);
  }

  @ExceptionHandler(BillingServiceUnavailableException.class)
  public ResponseEntity<Map<String, String>> handleBillingServiceUnavailableException(
      BillingServiceUnavailableException ex) {
    log.error("Patient creation aborted: {}", ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    errors.put("message",
        "Billing service unavailable, patient was not created. Please retry later");
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errors);
  }
}
