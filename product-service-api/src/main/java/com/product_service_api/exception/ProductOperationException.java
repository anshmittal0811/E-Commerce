package com.product_service_api.exception;

/**
 * Exception thrown when an error occurs during product operations.
 * This exception indicates that a product operation (save, update, delete) failed
 * due to an underlying issue such as validation failure or persistence error.
 */
public class ProductOperationException extends RuntimeException {

    /**
     * Constructs a new ProductOperationException with the specified message.
     *
     * @param message the detail message
     */
    public ProductOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ProductOperationException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ProductOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}

