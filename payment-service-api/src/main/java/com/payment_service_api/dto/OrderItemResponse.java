package com.payment_service_api.dto;

import lombok.*;

/**
 * Data Transfer Object for order item information.
 * 
 * <p>Represents a single line item in an order with
 * product details, quantity, and pricing information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    /**
     * The unique identifier of the order item.
     */
    private Long id;

    /**
     * The product ID associated with this item.
     */
    private Long idProduct;

    /**
     * The name of the product.
     */
    private String nameProduct;

    /**
     * The quantity of this product in the order.
     */
    private Integer quantity;

    /**
     * The unit price of the product.
     */
    private Double unitPrice;

    /**
     * The total price for this line item (quantity * unitPrice).
     */
    private Double totalPrice;

}
