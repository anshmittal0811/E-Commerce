package com.product_service_api.exception;

/**
 * Exception thrown when attempting to retrieve a product that does not exist in the system.
 * This exception indicates that the requested product ID does not correspond to any existing product.
 */
public class ProductNotFoundException extends RuntimeException {

    /**
     * Constructs a new ProductNotFoundException with the specified product ID.
     *
     * @param productId the product ID that was not found
     */
    public ProductNotFoundException(Long productId) {
        super(String.format("Product not found with ID: %d", productId));
    }

    /**
     * Constructs a new ProductNotFoundException with the specified product ID and cause.
     *
     * @param productId the product ID that was not found
     * @param cause the cause of this exception
     */
    public ProductNotFoundException(Long productId, Throwable cause) {
        super(String.format("Product not found with ID: %d", productId), cause);
    }

    /**
     * Constructs a new ProductNotFoundException with a custom message.
     *
     * @param message the detail message
     */
    public ProductNotFoundException(String message) {
        super(message);
    }

}

