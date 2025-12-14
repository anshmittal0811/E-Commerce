package com.order_service_api.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.order_service_api.dto.OrderResponse;
import com.order_service_api.entity.Order;
import com.order_service_api.exception.OrderCreationException;
import com.order_service_api.exception.OrderNotFoundException;
import com.order_service_api.model.CurrentUser;
import com.order_service_api.service.OrderService;

/**
 * REST controller for order management operations.
 * 
 * <p>This controller provides endpoints for:
 * <ul>
 *   <li>Creating new orders from user carts</li>
 *   <li>Processing and completing orders</li>
 *   <li>Retrieving orders by user or all orders (admin only)</li>
 * </ul>
 * 
 * <p>All endpoints require authentication. Admin-only endpoints are protected
 * with @PreAuthorize annotations.
 * 
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates a new order for the authenticated user.
     * 
     * <p>This endpoint retrieves the user's cart and creates an order with PENDING status.
     * The cart is cleared after successful order creation.
     * 
     * @param auth the Spring Security authentication object
     * @param request the HTTP request containing user headers
     * @return ResponseEntity containing the created order or error message
     */
    @Operation(
        summary = "Create a new order",
        description = "Creates a new order from the authenticated user's shopping cart"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Order created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or empty cart"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Order>> createOrder(Authentication auth, HttpServletRequest request) {
        CurrentUser user = buildCurrentUser(auth, request);

        log.info("Order creation request received for user ID: {}", user.getUserId());

        // Validate user ID
        if (user.getUserId() == null) {
            log.error("Order creation failed: User ID is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "User ID is required", null));
        }

        try {
            Order newOrder = orderService.createOrder(user.getUserId());

            log.info("Order created successfully with ID: {} for user ID: {}", 
                    newOrder.getOrderId(), user.getUserId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Order created successfully", newOrder));

        } catch (OrderCreationException e) {
            log.warn("Order creation failed for user ID: {} - {}", user.getUserId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (IllegalArgumentException e) {
            log.error("Order creation failed: Invalid request - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error during order creation for user ID: {}", user.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while creating the order", null));
        }
    }

    /**
     * Updates an order status to PROCESSING.
     * 
     * <p>This endpoint marks an order as being processed/prepared.
     * 
     * @param orderId the unique identifier of the order to process
     * @return ResponseEntity containing the updated order or error message
     */
    @Operation(
        summary = "Process an order",
        description = "Updates an order status to PROCESSING"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order status updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/bring/{orderId}")
    public ResponseEntity<ApiResponse<Order>> bringOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        log.info("Bring order request received for order ID: {}", orderId);

        try {
            Order order = orderService.bringOrder(orderId);

            log.info("Order ID: {} status updated to PROCESSING", orderId);

            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Order status updated to PROCESSING", order));

        } catch (OrderNotFoundException e) {
            log.warn("Order not found with ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (IllegalArgumentException e) {
            log.error("Invalid order ID: {} - {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while processing order ID: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while processing the order", null));
        }
    }

    /**
     * Completes an order by updating its status to COMPLETED.
     * 
     * <p>This endpoint marks an order as fulfilled.
     * 
     * @param orderId the unique identifier of the order to complete
     * @return ResponseEntity containing the updated order or error message
     */
    @Operation(
        summary = "Complete an order",
        description = "Updates an order status to COMPLETED"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order completed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PostMapping("/complete/{orderId}")
    public ResponseEntity<ApiResponse<Order>> completeOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        log.info("Complete order request received for order ID: {}", orderId);

        try {
            Order order = orderService.completeOrder(orderId);

            log.info("Order ID: {} status updated to COMPLETED", orderId);

            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Order completed successfully", order));

        } catch (OrderNotFoundException e) {
            log.warn("Order not found with ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (IllegalArgumentException e) {
            log.error("Invalid order ID: {} - {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while completing order ID: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while completing the order", null));
        }
    }

    /**
     * Retrieves all orders placed by a specific user.
     * 
     * <p>This endpoint is restricted to ADMIN users only.
     * 
     * @param email the email address of the user whose orders to retrieve
     * @return ResponseEntity containing the list of orders or error message
     */
    @Operation(
        summary = "Get orders by user",
        description = "Retrieves all orders for a specific user (Admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orders retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/user/{email}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUser(
            @Parameter(description = "User email") @PathVariable String email) {
        log.info("Get orders request received for user email: {}", email);

        try {
            List<OrderResponse> orderResponses = orderService.getOrdersByUser(email);

            if (orderResponses.isEmpty()) {
                log.debug("No orders found for email: {}", email);
                return ResponseEntity.ok(
                        new ApiResponse<>("SUCCESS", "No orders found for user: " + email, orderResponses));
            }

            log.info("Successfully retrieved {} order(s) for email: {}", orderResponses.size(), email);

            return ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", "Orders retrieved successfully", orderResponses));

        } catch (IllegalArgumentException e) {
            log.error("Invalid email: {} - {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while retrieving orders for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while retrieving orders", null));
        }
    }

    /**
     * Retrieves all orders in the system.
     * 
     * <p>This endpoint is restricted to ADMIN users only.
     * 
     * @return ResponseEntity containing the list of all orders or error message
     */
    @Operation(
        summary = "Get all orders",
        description = "Retrieves all orders in the system (Admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orders retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        log.info("Get all orders request received");

        try {
            List<OrderResponse> orderResponses = orderService.getAllOrders();

            if (orderResponses.isEmpty()) {
                log.debug("No orders found in database");
                return ResponseEntity.ok(
                        new ApiResponse<>("SUCCESS", "No orders found", orderResponses));
            }

            log.info("Successfully retrieved {} order(s)", orderResponses.size());

            return ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", "Orders retrieved successfully", orderResponses));

        } catch (Exception e) {
            log.error("Unexpected error while retrieving all orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while retrieving orders", null));
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
