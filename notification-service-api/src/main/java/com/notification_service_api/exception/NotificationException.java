package com.notification_service_api.exception;

/**
 * Exception thrown when an error occurs during notification processing.
 * This exception indicates that a notification operation failed due to
 * invalid data, processing errors, or external service failures.
 */
public class NotificationException extends RuntimeException {

    /**
     * Constructs a new NotificationException with the specified message.
     *
     * @param message the detail message
     */
    public NotificationException(String message) {
        super(message);
    }

    /**
     * Constructs a new NotificationException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }

}

