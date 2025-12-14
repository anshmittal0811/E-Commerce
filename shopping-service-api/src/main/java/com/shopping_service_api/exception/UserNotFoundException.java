package com.shopping_service_api.exception;

/**
 * Exception thrown when attempting to access a user that does not exist.
 * This exception indicates that the requested user was not found via the User Service.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Constructs a new UserNotFoundException with the specified user ID.
     *
     * @param userId the user ID that was not found
     */
    public UserNotFoundException(Long userId) {
        super(String.format("User not found with ID: %d", userId));
    }

    /**
     * Constructs a new UserNotFoundException with a custom message.
     *
     * @param message the detail message
     */
    public UserNotFoundException(String message) {
        super(message);
    }

}

