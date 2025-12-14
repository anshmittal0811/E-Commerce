package com.payment_service_api.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

/**
 * Data Transfer Object for order information received from Order Service.
 * 
 * <p>Contains complete order details including user information,
 * order items, status, and totals needed for payment processing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    /**
     * The unique identifier of the order.
     */
    private Long orderId;

    /**
     * The first name of the user who placed the order.
     */
    private String name;

    /**
     * The last name of the user who placed the order.
     */
    private String lastName;

    /**
     * The email address of the user.
     */
    private String email;

    /**
     * The role of the user (e.g., ADMIN, CLIENT).
     */
    private String role;

    /**
     * The shipping address for the order.
     */
    private String address;

    /**
     * The phone number of the user.
     */
    private String phone;

    /**
     * List of items in the order.
     */
    private List<OrderItemResponse> items;

    /**
     * The total amount for the order.
     */
    private Double totalAmount;

    /**
     * The current status of the order.
     */
    private StatusOrder orderStatus;

    /**
     * The date and time the order was placed.
     */
    private LocalDateTime orderDate;

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
