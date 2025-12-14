package com.shopping_service_api.service;

import java.util.ArrayList;

import com.shopping_service_api.exception.CartNotFoundException;
import com.shopping_service_api.exception.CartOperationException;
import com.shopping_service_api.exception.ProductNotFoundException;
import com.shopping_service_api.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shopping_service_api.client.ProductServiceClient;
import com.shopping_service_api.client.UserServiceClient;
import com.shopping_service_api.dto.ProductResponse;
import com.shopping_service_api.dto.UserResponse;
import com.shopping_service_api.entity.Cart;
import com.shopping_service_api.entity.CartItem;
import com.shopping_service_api.repository.CartRepository;

/**
 * Implementation of the ShoppingService interface.
 * 
 * <p>This service handles all shopping cart business logic including:
 * <ul>
 *   <li>Adding products to the cart with stock validation</li>
 *   <li>Removing products from the cart</li>
 *   <li>Retrieving cart contents</li>
 *   <li>Clearing the cart</li>
 * </ul>
 * 
 * <p>Integrates with Product Service and User Service via Feign clients.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingServiceImpl implements ShoppingService {

    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;
    private final CartRepository cartRepository;

    /**
     * Adds a product to the user's shopping cart.
     * 
     * <p>This method:
     * <ul>
     *   <li>Validates product existence and stock availability</li>
     *   <li>Creates a new cart if user doesn't have one</li>
     *   <li>Updates quantity if product already exists in cart</li>
     *   <li>Updates product stock via Product Service</li>
     *   <li>Recalculates cart total</li>
     * </ul>
     * 
     * @param idUser the user ID
     * @param idProduct the product ID to add
     * @param quantity the quantity to add
     * @return the updated cart
     * @throws ProductNotFoundException if the product does not exist
     * @throws UserNotFoundException if the user does not exist
     * @throws CartOperationException if there is insufficient stock
     */
    @Override
    @Transactional
    public Cart addToCart(Long idUser, Long idProduct, Integer quantity) {
        log.debug("Adding product to cart - User ID: {}, Product ID: {}, Quantity: {}",
                idUser, idProduct, quantity);

        // Fetch and validate product
        ProductResponse product = productServiceClient.findProductById(idProduct);
        if (product == null) {
            log.warn("Product not found with ID: {}", idProduct);
            throw new ProductNotFoundException(idProduct);
        }

        log.debug("Product found: {} (Stock: {})", product.getName(), product.getStock());

        // Validate stock availability
        Integer stock = product.getStock();
        if (stock < quantity) {
            log.warn("Insufficient stock for product ID: {}. Available: {}, Requested: {}",
                    idProduct, stock, quantity);
            throw new CartOperationException(
                    String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                            product.getName(), stock, quantity));
        }

        // Fetch and validate user
        UserResponse user = userServiceClient.getUserById(idUser);
        if (user == null) {
            log.warn("User not found with ID: {}", idUser);
            throw new UserNotFoundException(idUser);
        }

        log.debug("User found: {} ({})", user.getName(), user.getEmail());

        // Get or create cart
        Cart cart = cartRepository.findByIdUser(idUser);
        if (cart == null) {
            log.debug("Creating new cart for user ID: {}", idUser);
            cart = Cart.builder()
                    .idUser(user.getId())
                    .email(user.getEmail())
                    .total(0.0)
                    .cartItems(new ArrayList<>())
                    .build();
        }

        // Check if product already exists in cart
        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getIdProduct().equals(idProduct))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update existing item quantity
            int newQuantity = existingItem.getQuantity() + quantity;
            log.debug("Updating existing cart item quantity: {} -> {}", existingItem.getQuantity(), newQuantity);
            existingItem.setQuantity(newQuantity);
        } else {
            // Create new cart item
            log.debug("Adding new item to cart: {}", product.getName());
            CartItem newItem = CartItem.builder()
                    .idProduct(idProduct)
                    .nameProduct(product.getName())
                    .quantity(quantity)
                    .unitPrice(product.getPrice())
                    .cart(cart)
                    .build();
            cart.getCartItems().add(newItem);
        }

        // Update product stock
        try {
            productServiceClient.updateStockProduct(idProduct, quantity);
            log.debug("Product stock updated successfully for product ID: {}", idProduct);
        } catch (Exception e) {
            log.error("Failed to update product stock for product ID: {}", idProduct, e);
            throw new CartOperationException("Failed to update product stock", e);
        }

        // Recalculate cart total
        Double newTotal = calculateCartTotal(cart);
        cart.setTotal(newTotal);

        log.info("Product added to cart - User ID: {}, Product: {}, Quantity: {}, New Total: {}",
                idUser, product.getName(), quantity, newTotal);

        return cartRepository.save(cart);
    }

    /**
     * Removes a product from the user's shopping cart.
     * 
     * <p>This method removes the entire product entry from the cart,
     * regardless of quantity, and recalculates the cart total.
     * 
     * @param idUser the user ID
     * @param idProduct the product ID to remove
     * @return the updated cart
     * @throws CartNotFoundException if the user's cart does not exist
     * @throws CartOperationException if the product is not in the cart
     */
    @Override
    @Transactional
    public Cart removeFromCart(Long idUser, Long idProduct) {
        log.debug("Removing product from cart - User ID: {}, Product ID: {}", idUser, idProduct);

        // Find cart
        Cart cart = cartRepository.findByIdUser(idUser);
        if (cart == null) {
            log.warn("Cart not found for user ID: {}", idUser);
            throw new CartNotFoundException(idUser);
        }

        // Find item to remove
        CartItem itemToRemove = cart.getCartItems().stream()
                .filter(item -> item.getIdProduct().equals(idProduct))
                .findFirst()
                .orElse(null);

        if (itemToRemove == null) {
            log.warn("Product ID: {} not found in cart for user ID: {}", idProduct, idUser);
            throw new CartOperationException(
                    String.format("Product with ID %d is not in the cart", idProduct));
        }

        // Remove item and recalculate total
        cart.getCartItems().remove(itemToRemove);
        Double newTotal = calculateCartTotal(cart);
        cart.setTotal(newTotal);

        log.info("Product removed from cart - User ID: {}, Product ID: {}, New Total: {}",
                idUser, idProduct, newTotal);

        return cartRepository.save(cart);
    }

    /**
     * Retrieves the shopping cart for a specific user.
     * 
     * @param idUser the user ID
     * @return the user's cart
     * @throws CartNotFoundException if the user's cart does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public Cart sendCart(Long idUser) {
        log.debug("Retrieving cart for user ID: {}", idUser);

        Cart cart = cartRepository.findByIdUser(idUser);
        if (cart == null) {
            log.warn("Cart not found for user ID: {}", idUser);
            throw new CartNotFoundException(idUser);
        }

        log.debug("Cart retrieved - User ID: {}, Items: {}, Total: {}",
                idUser, cart.getCartItems().size(), cart.getTotal());

        return cart;
    }

    /**
     * Clears all items from the user's shopping cart.
     * 
     * <p>This method removes all items and resets the cart total to zero.
     * 
     * @param idUser the user ID
     * @throws CartNotFoundException if the user's cart does not exist
     */
    @Override
    @Transactional
    public void clearCart(Long idUser) {
        log.debug("Clearing cart for user ID: {}", idUser);

        Cart cart = cartRepository.findByIdUser(idUser);
        if (cart == null) {
            log.warn("Cart not found for user ID: {}", idUser);
            throw new CartNotFoundException(idUser);
        }

        int itemCount = cart.getCartItems().size();
        cart.setTotal(0.0);
        cart.getCartItems().clear();
        cartRepository.save(cart);

        log.info("Cart cleared - User ID: {}, Items removed: {}", idUser, itemCount);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Calculates the total price of all items in the cart.
     * 
     * @param cart the cart to calculate total for
     * @return the calculated total
     */
    private Double calculateCartTotal(Cart cart) {
        return cart.getCartItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
    }

}
