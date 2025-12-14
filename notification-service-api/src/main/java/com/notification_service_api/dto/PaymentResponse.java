package com.notification_service_api.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for payment notification data.
 * 
 * <p>Contains all information needed to send order confirmation
 * notifications via email and SMS, including user details,
 * order information, and payment status.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    /**
     * The order ID associated with the payment.
     */
    private Long orderId;

    /**
     * The full name of the user who placed the order.
     */
    private String userName;

    /**
     * The email address for sending confirmation emails.
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
     * The current status of the order.
     */
    private StatusOrder orderStatus;

    /**
     * The date and time the order was placed.
     */
    private LocalDateTime orderDate;

    /**
     * The total amount paid for the order.
     */
    private Double totalAmount;

    /**
     * Enumeration of possible order statuses.
     */
    public enum StatusOrder {
        /** Order created but not yet processed */
        PENDING,
        /** Order is being processed/prepared */
        PROCESSING,
        /** Order has been completed and paid */
        COMPLETED
    }

}
