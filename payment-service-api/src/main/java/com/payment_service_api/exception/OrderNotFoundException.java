package com.payment_service_api.exception;

/**
 * Exception thrown when attempting to access an order that does not exist.
 * This exception indicates that the requested order was not found via the Order Service.
 */
public class OrderNotFoundException extends RuntimeException {

    /**
     * Constructs a new OrderNotFoundException with the specified order ID.
     *
     * @param orderId the order ID that was not found
     */
    public OrderNotFoundException(Long orderId) {
        super(String.format("Order not found with ID: %d", orderId));
    }

    /**
     * Constructs a new OrderNotFoundException with a custom message.
     *
     * @param message the detail message
     */
    public OrderNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new OrderNotFoundException with the specified order ID and cause.
     *
     * @param orderId the order ID that was not found
     * @param cause the cause of this exception
     */
    public OrderNotFoundException(Long orderId, Throwable cause) {
        super(String.format("Order not found with ID: %d", orderId), cause);
    }

}

