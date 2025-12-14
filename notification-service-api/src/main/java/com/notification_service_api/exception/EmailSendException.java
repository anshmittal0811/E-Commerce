package com.notification_service_api.exception;

/**
 * Exception thrown when an error occurs while sending an email.
 * This exception indicates that email delivery failed due to
 * configuration issues, invalid recipients, or mail server errors.
 */
public class EmailSendException extends RuntimeException {

    /**
     * Constructs a new EmailSendException with the specified message.
     *
     * @param message the detail message
     */
    public EmailSendException(String message) {
        super(message);
    }

    /**
     * Constructs a new EmailSendException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }

}

