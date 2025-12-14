package com.payment_service_api.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity class representing a payment transaction.
 * 
 * <p>This entity maps to the 'payment' table and contains
 * all payment-related information including order reference,
 * amount, currency, method, and status.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    /**
     * Unique identifier for the payment.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The ID of the order this payment is for.
     */
    @Column(nullable = false)
    private Long orderId;

    /**
     * The total payment amount.
     */
    @Column(nullable = false)
    private Double total;

    /**
     * The currency code (e.g., USD, EUR, INR).
     */
    @Column(nullable = false)
    private String currency;

    /**
     * The payment method used (e.g., CARD, PAYPAL, UPI).
     */
    @Column(nullable = false)
    private String method;

    /**
     * Optional description or notes for the payment.
     */
    private String description;

    /**
     * The status of the payment (e.g., SUCCESS, FAILED, PENDING).
     */
    @Column(nullable = false)
    private String status;

}
