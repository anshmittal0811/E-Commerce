package com.shopping_service_api.service;

import com.shopping_service_api.entity.Cart;

/**
 * Service interface for shopping cart operations.
 * 
 * <p>This interface defines the contract for:
 * <ul>
 *   <li>Adding products to the shopping cart</li>
 *   <li>Removing products from the shopping cart</li>
 *   <li>Retrieving cart contents</li>
 *   <li>Clearing the shopping cart</li>
 * </ul>
 * 
 * <p>Implementations should handle proper validation and exception throwing.
 */
public interface ShoppingService {

    /**
     * Adds a product to the user's shopping cart.
     * 
     * @param idUser the user ID
     * @param idProduct the product ID to add
     * @param quantity the quantity to add
     * @return the updated cart
     */
    Cart addToCart(Long idUser, Long idProduct, Integer quantity);

    /**
     * Removes a product from the user's shopping cart.
     * 
     * @param idUser the user ID
     * @param idProduct the product ID to remove
     * @return the updated cart
     */
    Cart removeFromCart(Long idUser, Long idProduct);

    /**
     * Retrieves the shopping cart for a specific user.
     * 
     * @param idUser the user ID
     * @return the user's cart
     */
    Cart sendCart(Long idUser);

    /**
     * Clears all items from the user's shopping cart.
     * 
     * @param idUser the user ID
     */
    void clearCart(Long idUser);

}
