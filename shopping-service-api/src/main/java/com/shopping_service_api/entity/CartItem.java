package com.shopping_service_api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity class representing an item in a shopping cart.
 * 
 * <p>This entity maps to the 'cart_items' table and represents
 * a single product entry in a user's shopping cart with quantity
 * and price information.
 * 
 * <p>Cart items are associated with a parent Cart entity
 * and are managed through cascade operations.
 */
@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    /**
     * Unique identifier for the cart item.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the parent cart.
     * Uses JsonBackReference to prevent infinite recursion during serialization.
     */
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonBackReference
    private Cart cart;

    /**
     * The ID of the product in this cart item.
     * References the product in the Product Service.
     */
    private Long idProduct;

    /**
     * The name of the product.
     * Stored for quick display without additional service calls.
     */
    private String nameProduct;

    /**
     * The quantity of this product in the cart.
     */
    private Integer quantity;

    /**
     * The unit price of the product at time of adding to cart.
     * Stored to maintain price consistency even if product price changes.
     */
    private Double unitPrice;

}
