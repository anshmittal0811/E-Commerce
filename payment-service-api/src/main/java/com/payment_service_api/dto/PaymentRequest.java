package com.payment_service_api.dto;

import lombok.*;

import java.time.LocalDateTime;

import com.payment_service_api.dto.OrderResponse.StatusOrder;

/**
 * Data Transfer Object for payment notification messages.
 * 
 * <p>This DTO is sent to Kafka for notification processing
 * and contains order and payment information needed for
 * email and SMS notifications.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    /**
     * The order ID associated with the payment.
     */
    private Long orderId;

    /**
     * The full name of the user who placed the order.
     */
    private String userName;

    /**
     * The email address for sending payment confirmation.
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
     * The total amount paid.
     */
    private Double totalAmount;

}
