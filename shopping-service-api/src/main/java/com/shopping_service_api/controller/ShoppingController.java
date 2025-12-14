package com.shopping_service_api.controller;

import com.shopping_service_api.exception.CartNotFoundException;
import com.shopping_service_api.exception.CartOperationException;
import com.shopping_service_api.exception.ProductNotFoundException;
import com.shopping_service_api.exception.UserNotFoundException;
import com.shopping_service_api.model.CurrentUser;
import com.shopping_service_api.dto.ProductRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.shopping_service_api.entity.Cart;
import com.shopping_service_api.service.ShoppingService;

/**
 * REST controller for shopping cart management operations.
 * 
 * <p>This controller provides endpoints for:
 * <ul>
 *   <li>Adding products to the shopping cart</li>
 *   <li>Removing products from the shopping cart</li>
 *   <li>Retrieving the current cart contents</li>
 *   <li>Clearing the shopping cart</li>
 * </ul>
 * 
 * <p>All endpoints require authentication. User identity is extracted
 * from either Spring Security authentication or HTTP headers.
 */
@RestController
@RequestMapping("/shopping")
@RequiredArgsConstructor
@Slf4j
public class ShoppingController {

    private final ShoppingService shoppingService;

    /**
     * Adds a product to the authenticated user's shopping cart.
     * 
     * <p>This endpoint validates product availability and stock before adding.
     * If the product already exists in the cart, the quantity is increased.
     * 
     * @param auth the Spring Security authentication object
     * @param request the HTTP request containing user headers
     * @param productRequest the product ID and quantity to add
     * @return ResponseEntity containing the updated cart or error message
     */
    @PostMapping("/add-to-cart")
    public ResponseEntity<ApiResponse<Cart>> addToCart(
            Authentication auth,
            HttpServletRequest request,
            @RequestBody ProductRequest productRequest) {

        CurrentUser user = buildCurrentUser(auth, request);

        log.info("Add to cart request received - User ID: {}, Product ID: {}, Quantity: {}",
                user.getUserId(), productRequest.getIdProduct(), productRequest.getQuantity());

        // Validate user ID
        if (user.getUserId() == null) {
            log.error("Add to cart failed: User ID is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "User ID is required", null));
        }

        // Validate product request
        if (productRequest.getIdProduct() == null) {
            log.error("Add to cart failed: Product ID is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Product ID is required", null));
        }

        if (productRequest.getQuantity() == null || productRequest.getQuantity() <= 0) {
            log.error("Add to cart failed: Invalid quantity: {}", productRequest.getQuantity());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Quantity must be a positive number", null));
        }

        try {
            Cart cart = shoppingService.addToCart(
                    user.getUserId(),
                    productRequest.getIdProduct(),
                    productRequest.getQuantity());

            log.info("Product added to cart successfully - User ID: {}, Product ID: {}, Cart Total: {}",
                    user.getUserId(), productRequest.getIdProduct(), cart.getTotal());

            return ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", "Product added to cart successfully", cart));

        } catch (ProductNotFoundException e) {
            log.warn("Add to cart failed: Product not found - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (UserNotFoundException e) {
            log.warn("Add to cart failed: User not found - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (CartOperationException e) {
            log.warn("Add to cart failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while adding product to cart - User ID: {}", user.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while adding product to cart", null));
        }
    }

    /**
     * Removes a product from the authenticated user's shopping cart.
     * 
     * <p>This endpoint removes the entire product entry from the cart,
     * regardless of quantity.
     * 
     * @param auth the Spring Security authentication object
     * @param request the HTTP request containing user headers
     * @param productRequest the product ID to remove
     * @return ResponseEntity containing the updated cart or error message
     */
    @DeleteMapping("/remove-from-cart")
    public ResponseEntity<ApiResponse<Cart>> removeFromCart(
            Authentication auth,
            HttpServletRequest request,
            @RequestBody ProductRequest productRequest) {

        CurrentUser user = buildCurrentUser(auth, request);

        log.info("Remove from cart request received - User ID: {}, Product ID: {}",
                user.getUserId(), productRequest.getIdProduct());

        // Validate user ID
        if (user.getUserId() == null) {
            log.error("Remove from cart failed: User ID is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "User ID is required", null));
        }

        // Validate product ID
        if (productRequest.getIdProduct() == null) {
            log.error("Remove from cart failed: Product ID is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Product ID is required", null));
        }

        try {
            Cart cart = shoppingService.removeFromCart(user.getUserId(), productRequest.getIdProduct());

            log.info("Product removed from cart successfully - User ID: {}, Product ID: {}, Cart Total: {}",
                    user.getUserId(), productRequest.getIdProduct(), cart.getTotal());

            return ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", "Product removed from cart successfully", cart));

        } catch (CartNotFoundException e) {
            log.warn("Remove from cart failed: Cart not found - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (CartOperationException e) {
            log.warn("Remove from cart failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while removing product from cart - User ID: {}", user.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while removing product from cart", null));
        }
    }

    /**
     * Retrieves the current shopping cart for the authenticated user.
     * 
     * <p>This endpoint returns the complete cart with all items and total.
     * 
     * @param auth the Spring Security authentication object
     * @param request the HTTP request containing user headers
     * @return ResponseEntity containing the cart or error message
     */
    @GetMapping("/send-cart")
    public ResponseEntity<ApiResponse<Cart>> sendCart(Authentication auth, HttpServletRequest request) {
        CurrentUser user = buildCurrentUser(auth, request);

        log.info("Get cart request received - User ID: {}", user.getUserId());

        // Validate user ID
        if (user.getUserId() == null) {
            log.error("Get cart failed: User ID is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "User ID is required", null));
        }

        try {
            Cart cart = shoppingService.sendCart(user.getUserId());

            log.info("Cart retrieved successfully - User ID: {}, Items: {}, Total: {}",
                    user.getUserId(), cart.getCartItems().size(), cart.getTotal());

            return ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", "Cart retrieved successfully", cart));

        } catch (CartNotFoundException e) {
            log.warn("Get cart failed: Cart not found - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while retrieving cart - User ID: {}", user.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while retrieving cart", null));
        }
    }

    /**
     * Clears all items from the authenticated user's shopping cart.
     * 
     * <p>This endpoint removes all items and resets the cart total to zero.
     * 
     * @param auth the Spring Security authentication object
     * @param request the HTTP request containing user headers
     * @return ResponseEntity with success message or error
     */
    @GetMapping("/clear-cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication auth, HttpServletRequest request) {
        CurrentUser user = buildCurrentUser(auth, request);

        log.info("Clear cart request received - User ID: {}", user.getUserId());

        // Validate user ID
        if (user.getUserId() == null) {
            log.error("Clear cart failed: User ID is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "User ID is required", null));
        }

        try {
            shoppingService.clearCart(user.getUserId());

            log.info("Cart cleared successfully - User ID: {}", user.getUserId());

            return ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", "Cart cleared successfully", null));

        } catch (CartNotFoundException e) {
            log.warn("Clear cart failed: Cart not found - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while clearing cart - User ID: {}", user.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while clearing cart", null));
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * Builds a CurrentUser object from authentication and request headers.
     * 
     * <p>Extracts user information from:
     * <ul>
     *   <li>Spring Security Authentication object (if available)</li>
     *   <li>HTTP request headers as fallback (X-USER-EMAIL, X-USER-ROLE, X-USER-ID)</li>
     * </ul>
     * 
     * @param auth the Spring Security authentication object
     * @param request the HTTP request containing user headers
     * @return CurrentUser object with user identity information
     */
    private CurrentUser buildCurrentUser(Authentication auth, HttpServletRequest request) {
        // Extract email from auth or header
        String email = (auth != null) ? auth.getName() : request.getHeader("X-USER-EMAIL");

        // Extract role from auth or header
        String role = (auth != null && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority()
                : request.getHeader("X-USER-ROLE");

        // Extract user ID from header
        String userIdHeader = request.getHeader("X-USER-ID");
        Long userId = (userIdHeader != null) ? Long.valueOf(userIdHeader) : null;

        log.debug("Built CurrentUser - email: {}, role: {}, userId: {}", email, role, userId);

        return new CurrentUser(email, role, userId);
    }

    // ==================== Response Record ====================

    /**
     * Generic API response structure for all endpoints.
     * 
     * @param <T> the type of data contained in the response
     * @param status the status of the operation (SUCCESS or ERROR)
     * @param message the response message
     * @param data the response data (null if error)
     */
    public record ApiResponse<T>(String status, String message, T data) {}

}
