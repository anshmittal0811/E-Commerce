package com.shopping_service_api.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity class representing a shopping cart.
 * 
 * <p>This entity maps to the 'carts' table and represents a user's
 * shopping cart containing multiple cart items.
 * 
 * <p>Each user can have only one cart (unique constraint on idUser).
 * The cart maintains a total price calculated from all items.
 */
@Entity
@Table(name = "carts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    /**
     * Unique identifier for the cart.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The ID of the user who owns this cart.
     * Must be unique - one cart per user.
     */
    @Column(unique = true)
    private Long idUser;

    /**
     * The email address of the cart owner.
     * Stored for quick reference without additional lookups.
     */
    private String email;

    /**
     * The total price of all items in the cart.
     * Calculated as sum of (quantity * unitPrice) for each item.
     */
    private Double total;

    /**
     * List of items in the shopping cart.
     * Cascade operations and orphan removal are enabled.
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CartItem> cartItems;

}
