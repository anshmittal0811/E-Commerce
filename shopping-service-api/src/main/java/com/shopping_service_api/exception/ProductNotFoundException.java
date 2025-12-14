package com.shopping_service_api.exception;

/**
 * Exception thrown when attempting to access a product that does not exist.
 * This exception indicates that the requested product was not found via the Product Service.
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
     * Constructs a new ProductNotFoundException with a custom message.
     *
     * @param message the detail message
     */
    public ProductNotFoundException(String message) {
        super(message);
    }

}

