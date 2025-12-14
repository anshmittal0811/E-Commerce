package com.shopping_service_api.exception;

/**
 * Exception thrown when an error occurs during cart operations.
 * This exception indicates that a cart operation (add, remove, clear) failed
 * due to an underlying issue such as insufficient stock, invalid product, or persistence error.
 */
public class CartOperationException extends RuntimeException {

    /**
     * Constructs a new CartOperationException with the specified message.
     *
     * @param message the detail message
     */
    public CartOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a new CartOperationException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public CartOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}

