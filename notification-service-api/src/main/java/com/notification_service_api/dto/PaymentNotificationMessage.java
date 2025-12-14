package com.notification_service_api.dto;


import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for deserializing payment notification messages from Kafka.
 * 
 * <p>
 * This class matches the structure of PaymentRequest sent by
 * the Payment Service producer.
 */
@Getter
@Setter
public class PaymentNotificationMessage {

    /**
     * The order ID associated with the payment.
     */
    private Long orderId;

    /**
     * The full name of the user.
     */
    private String userName;

    /**
     * The email address for notifications.
     */
    private String userEmail;

    /**
     * The shipping address of the user.
     */
    private String userAddress;

    /**
     * The phone number for SMS notifications.
     */
    private String userPhone;

    /**
     * The order status as a string.
     */
    private String orderStatus;

    /**
     * The date and time of the order.
     */
    private LocalDateTime orderDate;

    /**
     * The total amount paid.
     */
    private Double totalAmount;

    @Override
    public String toString() {
        return String.format("PaymentNotificationMessage{orderId=%d, userName='%s', userEmail='%s', totalAmount=%.2f}",
                orderId, userName, userEmail, totalAmount != null ? totalAmount : 0.0);
    }
}