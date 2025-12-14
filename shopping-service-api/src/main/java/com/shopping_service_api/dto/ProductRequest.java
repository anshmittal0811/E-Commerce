package com.shopping_service_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for product-related requests.
 * 
 * <p>Used in shopping cart operations to specify which product
 * and what quantity to add or remove from the cart.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

    /**
     * The unique identifier of the product.
     */
    private Long idProduct;

    /**
     * The quantity for the operation (add/remove).
     */
    private Integer quantity;

}
