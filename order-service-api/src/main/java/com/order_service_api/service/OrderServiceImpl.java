package com.order_service_api.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.order_service_api.client.ShopServiceClient;
import com.order_service_api.client.UserServiceClient;
import com.order_service_api.dto.ApiResponse;
import com.order_service_api.dto.CartResponse;
import com.order_service_api.dto.OrderResponse;
import com.order_service_api.dto.UserResponse;
import com.order_service_api.entity.Order;
import com.order_service_api.entity.Order.StatusOrder;
import com.order_service_api.entity.OrderItem;
import com.order_service_api.exception.OrderCreationException;
import com.order_service_api.exception.OrderNotFoundException;
import com.order_service_api.repository.OrderRepository;

/**
 * Implementation of {@link OrderService} providing order management functionality.
 * 
 * <p>This service handles:
 * <ul>
 *   <li>Creating new orders from user carts</li>
 *   <li>Updating order status (processing, completed)</li>
 *   <li>Retrieving orders by user email</li>
 *   <li>Retrieving all orders in the system</li>
 * </ul>
 * 
 * <p>All operations are logged for audit and debugging purposes.
 * 
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final double ROUNDING_FACTOR = 100.0;

    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;
    private final ShopServiceClient shopServiceClient;
    private final ModelMapper modelMapper;

    /**
     * Creates a new order for the specified user based on their current cart.
     * 
     * <p>This method:
     * <ol>
     *   <li>Validates the user ID</li>
     *   <li>Retrieves the user's cart from the shop service</li>
     *   <li>Retrieves user details from the user service</li>
     *   <li>Maps cart items to order items with calculated prices</li>
     *   <li>Creates and persists the order with PENDING status</li>
     *   <li>Cleans the user's cart after successful order creation</li>
     * </ol>
     * 
     * @param idUser the unique identifier of the user placing the order
     * @return the created Order entity
     * @throws IllegalArgumentException if the user ID is null or invalid
     * @throws OrderCreationException if the order cannot be created due to external service failure
     */
    @Override
    @Transactional
    public Order createOrder(Long idUser) {
        log.debug("Create order request received for user ID: {}", idUser);

        // Validate input
        validateUserId(idUser);

        log.info("Processing order creation for user ID: {}", idUser);

        try {
            // Retrieve cart from shop service
            log.debug("Fetching cart for user ID: {}", idUser);
            CartResponse cart = fetchCart(idUser);

            if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                log.warn("Order creation failed: Cart is empty for user ID: {}", idUser);
                throw new OrderCreationException("Cannot create order: Cart is empty");
            }

            log.debug("Cart retrieved with {} item(s), total: {}", 
                    cart.getCartItems().size(), cart.getTotal());

            // Retrieve user details from user service
            log.debug("Fetching user details for user ID: {}", idUser);
            UserResponse user = userServiceClient.getUserById(idUser);

            if (user == null) {
                log.warn("Order creation failed: User not found for ID: {}", idUser);
                throw new OrderCreationException("Cannot create order: User not found");
            }

            log.debug("User retrieved: {}", user.getEmail());

            // Map cart items to order items with calculated total prices
            List<OrderItem> orderItems = cart.getCartItems().stream()
                    .map(cartItem -> {
                        OrderItem orderItem = modelMapper.map(cartItem, OrderItem.class);
                        orderItem.setId(null);
                        orderItem.setTotalPrice(roundToTwoDecimals(
                                cartItem.getUnitPrice() * cartItem.getQuantity()));
                        return orderItem;
                    })
                    .collect(Collectors.toList());

            log.debug("Mapped {} cart item(s) to order items", orderItems.size());

            // Create order entity from user details
            Order order = modelMapper.map(user, Order.class);
            order.setItems(orderItems);
            order.setTotalAmount(roundToTwoDecimals(cart.getTotal()));
            order.setOrderStatus(StatusOrder.PENDING);

            // Persist order to database
            Order savedOrder = orderRepository.save(order);
            log.debug("Order persisted with ID: {}", savedOrder.getOrderId());

            // Clean cart after successful order creation
            clearCart(idUser);
            log.debug("Cart cleaned for user ID: {}", idUser);

            log.info("Order successfully created with ID: {} for user ID: {}, total amount: {}", 
                    savedOrder.getOrderId(), idUser, savedOrder.getTotalAmount());

            return savedOrder;

        } catch (OrderCreationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while creating order for user ID: {}", idUser, e);
            throw new OrderCreationException("Failed to create order: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an order status to PROCESSING, indicating the order is being prepared.
     * 
     * <p>This method:
     * <ol>
     *   <li>Validates the order ID</li>
     *   <li>Retrieves the order from the database</li>
     *   <li>Updates the status to PROCESSING</li>
     *   <li>Persists the updated order</li>
     * </ol>
     * 
     * @param orderId the unique identifier of the order to process
     * @return the updated Order entity with PROCESSING status
     * @throws IllegalArgumentException if the order ID is null or invalid
     * @throws OrderNotFoundException if no order exists with the provided ID
     */
    @Override
    @Transactional
    public Order bringOrder(Long orderId) {
        log.debug("Bring order request received for order ID: {}", orderId);

        // Validate input
        validateOrderId(orderId);

        log.info("Processing bring order request for order ID: {}", orderId);

        // Retrieve order from database
        Order order = findOrderById(orderId);

        log.debug("Order found with current status: {}", order.getOrderStatus());

        // Update order status to PROCESSING
        order.setOrderStatus(StatusOrder.PROCESSING);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order ID: {} status updated to PROCESSING", orderId);

        return updatedOrder;
    }

    /**
     * Completes an order by updating its status to COMPLETED.
     * 
     * <p>This method:
     * <ol>
     *   <li>Validates the order ID</li>
     *   <li>Retrieves the order from the database</li>
     *   <li>Updates the status to COMPLETED</li>
     *   <li>Persists the updated order</li>
     * </ol>
     * 
     * @param orderId the unique identifier of the order to complete
     * @return the updated Order entity with COMPLETED status
     * @throws IllegalArgumentException if the order ID is null or invalid
     * @throws OrderNotFoundException if no order exists with the provided ID
     */
    @Override
    @Transactional
    public Order completeOrder(Long orderId) {
        log.debug("Complete order request received for order ID: {}", orderId);

        // Validate input
        validateOrderId(orderId);

        log.info("Processing complete order request for order ID: {}", orderId);

        // Retrieve order from database
        Order order = findOrderById(orderId);

        log.debug("Order found with current status: {}", order.getOrderStatus());

        // Update order status to COMPLETED
        order.setOrderStatus(StatusOrder.COMPLETED);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order ID: {} status updated to COMPLETED", orderId);

        return updatedOrder;
    }

    /**
     * Retrieves all orders placed by a specific user identified by their email.
     * 
     * <p>This method queries the database for all orders associated with the given email
     * and returns them as a list of {@link OrderResponse} DTOs.
     * 
     * @param email the email address of the user whose orders to retrieve
     * @return a list of order responses, or an empty list if no orders exist
     * @throws IllegalArgumentException if the email is null or empty
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(String email) {
        log.debug("Get orders request received for email: {}", email);

        // Validate input
        validateEmail(email);

        log.info("Fetching orders for user email: {}", email);

        try {
            List<Order> orders = orderRepository.findByEmail(email);

            if (orders.isEmpty()) {
                log.debug("No orders found for email: {}", email);
                return Collections.emptyList();
            }

            List<OrderResponse> orderResponses = orders.stream()
                    .map(order -> modelMapper.map(order, OrderResponse.class))
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} order(s) for email: {}", orders.size(), email);

            return orderResponses;

        } catch (Exception e) {
            log.error("Error occurred while retrieving orders for email: {}", email, e);
            throw new RuntimeException("Failed to retrieve orders: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all orders in the system.
     * 
     * <p>This method queries the database for all orders and returns them as a list
     * of {@link OrderResponse} DTOs.
     * 
     * <p>Note: This operation can be resource-intensive for large order volumes.
     * Consider implementing pagination for production use.
     * 
     * @return a list of all order responses, or an empty list if no orders exist
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.debug("Get all orders request received");

        try {
            List<Order> orders = orderRepository.findAll();

            log.info("Successfully retrieved {} order(s) from database", orders.size());

            if (orders.isEmpty()) {
                log.debug("No orders found in database");
                return Collections.emptyList();
            }

            List<OrderResponse> orderResponses = orders.stream()
                    .map(order -> modelMapper.map(order, OrderResponse.class))
                    .collect(Collectors.toList());

            return orderResponses;

        } catch (Exception e) {
            log.error("Error occurred while retrieving all orders from database", e);
            throw new RuntimeException("Failed to retrieve orders: " + e.getMessage(), e);
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * Finds an order by its ID or throws an exception if not found.
     * 
     * @param orderId the order ID to search for
     * @return the found Order entity
     * @throws OrderNotFoundException if no order exists with the provided ID
     */
    private Order findOrderById(Long orderId) {
        return orderRepository.findById(Objects.requireNonNull(orderId, "Order ID cannot be null"))
                .orElseThrow(() -> {
                    log.warn("Order not found with ID: {}", orderId);
                    return new OrderNotFoundException(orderId);
                });
    }

    /**
     * Validates the user ID to ensure it is not null and is a positive number.
     * 
     * @param userId the user ID to validate
     * @throws IllegalArgumentException if the ID is null or invalid
     */
    private void validateUserId(Long userId) {
        if (userId == null) {
            log.error("User ID is null");
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (userId <= 0) {
            log.error("Invalid user ID: {}", userId);
            throw new IllegalArgumentException("User ID must be a positive number");
        }
    }

    /**
     * Validates the order ID to ensure it is not null and is a positive number.
     * 
     * @param orderId the order ID to validate
     * @throws IllegalArgumentException if the ID is null or invalid
     */
    private void validateOrderId(Long orderId) {
        if (orderId == null) {
            log.error("Order ID is null");
            throw new IllegalArgumentException("Order ID cannot be null");
        }

        if (orderId <= 0) {
            log.error("Invalid order ID: {}", orderId);
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
    }

    /**
     * Validates the email to ensure it is not null or empty.
     * 
     * @param email the email to validate
     * @throws IllegalArgumentException if the email is null or empty
     */
    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            log.error("Email is null or empty");
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
    }

    /**
     * Rounds a double value to two decimal places.
     * 
     * @param value the value to round
     * @return the rounded value
     */
    private double roundToTwoDecimals(double value) {
        return Math.round(value * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }

    /**
     * Fetches the cart from the Shopping Service and unwraps the API response.
     * 
     * @param idUser the user ID for logging purposes
     * @return the cart response
     * @throws OrderCreationException if the cart cannot be retrieved
     */
    private CartResponse fetchCart(Long idUser) {
        ApiResponse<CartResponse> response = shopServiceClient.sendCart();

        if (response == null) {
            log.warn("Null response from Shopping Service for user ID: {}", idUser);
            throw new OrderCreationException("Cannot create order: Failed to retrieve cart");
        }

        if (!response.isSuccess() || response.getData() == null) {
            log.warn("Failed to retrieve cart for user ID: {} - {}", idUser, response.getMessage());
            throw new OrderCreationException("Cannot create order: " + response.getMessage());
        }

        return response.getData();
    }

    /**
     * Clears the cart via the Shopping Service and handles the API response.
     * 
     * @param idUser the user ID for logging purposes
     */
    private void clearCart(Long idUser) {
        try {
            ApiResponse<Void> response = shopServiceClient.cleanCart();

            if (response == null || !response.isSuccess()) {
                String errorMsg = response != null ? response.getMessage() : "Unknown error";
                log.warn("Failed to clear cart for user ID: {} - {}", idUser, errorMsg);
                // Log warning but don't fail - order was already created
            }
        } catch (Exception e) {
            log.warn("Error clearing cart for user ID: {} - {}", idUser, e.getMessage());
            // Log warning but don't fail - order was already created
        }
    }

}