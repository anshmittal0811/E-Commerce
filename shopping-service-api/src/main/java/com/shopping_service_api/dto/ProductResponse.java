package com.shopping_service_api.dto;

import lombok.*;

/**
 * Data Transfer Object for product information received from Product Service.
 * 
 * <p>Contains product details needed for shopping cart operations
 * including pricing and stock information.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {

    /**
     * The unique identifier of the product.
     */
    private Long id;

    /**
     * The name of the product.
     */
    private String name;

    /**
     * The detailed description of the product.
     */
    private String description;

    /**
     * The price of the product.
     */
    private Double price;

    /**
     * The category of the product.
     */
    private String category;

    /**
     * The current stock quantity available.
     */
    private Integer stock;

}
