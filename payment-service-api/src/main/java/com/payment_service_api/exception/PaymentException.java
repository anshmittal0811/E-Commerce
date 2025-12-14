package com.payment_service_api.exception;

/**
 * Exception thrown when an error occurs during payment processing.
 * This exception indicates that a payment operation failed due to
 * validation errors, order issues, or external service failures.
 */
public class PaymentException extends RuntimeException {

    /**
     * Constructs a new PaymentException with the specified message.
     *
     * @param message the detail message
     */
    public PaymentException(String message) {
        super(message);
    }

    /**
     * Constructs a new PaymentException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }

}

