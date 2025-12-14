package com.shopping_service_api.exception;

/**
 * Exception thrown when attempting to access a cart that does not exist.
 * This exception indicates that the requested user's cart was not found in the system.
 */
public class CartNotFoundException extends RuntimeException {

    /**
     * Constructs a new CartNotFoundException with the specified user ID.
     *
     * @param userId the user ID whose cart was not found
     */
    public CartNotFoundException(Long userId) {
        super(String.format("Cart not found for user ID: %d", userId));
    }

    /**
     * Constructs a new CartNotFoundException with a custom message.
     *
     * @param message the detail message
     */
    public CartNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new CartNotFoundException with the specified user ID and cause.
     *
     * @param userId the user ID whose cart was not found
     * @param cause the cause of this exception
     */
    public CartNotFoundException(Long userId, Throwable cause) {
        super(String.format("Cart not found for user ID: %d", userId), cause);
    }

}

