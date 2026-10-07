package com.pm.patientservice.exception;

public class BillingServiceUnavailableException extends RuntimeException {

  public BillingServiceUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
