package com.payment_service_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.payment_service_api.dto.OrderResponse;
import com.payment_service_api.entity.Payment;
import com.payment_service_api.exception.OrderNotFoundException;
import com.payment_service_api.exception.PaymentException;
import com.payment_service_api.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for payment processing operations.
 * 
 * <p>This controller provides endpoints for:
 * <ul>
 *   <li>Viewing order details before payment</li>
 *   <li>Creating and processing payments</li>
 * </ul>
 * 
 * <p>Integrates with Order Service for order details and
 * triggers notifications via Kafka after successful payment.
 */
@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment processing endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Displays the payment page with order details.
     * 
     * <p>This endpoint retrieves order information and renders
     * the payment form for the user.
     * 
     * @param orderId the order ID to view
     * @param model the Spring MVC model for view rendering
     * @return the view name for the payment page
     */
    @Operation(summary = "View payment page", description = "Displays the payment page with order details")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> viewPayment(
            @Parameter(description = "Order ID") @PathVariable Long orderId, 
            Model model) {
        log.info("View payment request received for order ID: {}", orderId);

        try {
            OrderResponse orderResponse = paymentService.viewOrderDetails(orderId);

            log.debug("Order details loaded for payment view - Order ID: {}, Total: {}",
                    orderId, orderResponse.getTotalAmount());

            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Order details loaded successfully", orderResponse));

        } catch (OrderNotFoundException e) {
            log.warn("Order not found for payment view - Order ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (Exception e) {
            log.error("Error loading payment view for order ID: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An error occurred while loading order details", null));
        }
    }

    /**
     * Creates and processes a payment for an order.
     * 
     * <p>This endpoint:
     * <ul>
     *   <li>Validates payment parameters</li>
     *   <li>Creates a payment record</li>
     *   <li>Updates the order status to COMPLETED</li>
     *   <li>Sends a payment notification via Kafka</li>
     * </ul>
     * 
     * @param orderId the order ID to pay for
     * @param method the payment method (e.g., CARD, PAYPAL)
     * @param amount the payment amount
     * @param currency the currency code (e.g., USD, EUR)
     * @param description optional payment description
     * @return ResponseEntity containing the payment result or error
     */
    @Operation(summary = "Create payment", description = "Creates and processes a payment for an order")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payment data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Payment>> createPayment(
            @Parameter(description = "Order ID") @RequestParam("orderid") Long orderId,
            @Parameter(description = "Payment method (e.g., CARD, PAYPAL)") @RequestParam("method") String method,
            @Parameter(description = "Payment amount") @RequestParam("amount") String amount,
            @Parameter(description = "Currency code (e.g., USD, EUR)") @RequestParam("currency") String currency,
            @Parameter(description = "Payment description") @RequestParam("description") String description) {

        log.info("Create payment request received - Order ID: {}, Method: {}, Amount: {}, Currency: {}",
                orderId, method, amount, currency);

        // Validate order ID
        if (orderId == null || orderId <= 0) {
            log.error("Create payment failed: Invalid order ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Invalid order ID", null));
        }

        // Validate payment method
        if (method == null || method.trim().isEmpty()) {
            log.error("Create payment failed: Payment method is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Payment method is required", null));
        }

        // Validate and parse amount
        Double parsedAmount;
        try {
            String sanitizedAmount = amount.replace(',', '.');
            parsedAmount = Double.valueOf(sanitizedAmount);

            if (parsedAmount <= 0) {
                log.error("Create payment failed: Invalid amount: {}", amount);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>("ERROR", "Amount must be a positive value", null));
            }
        } catch (NumberFormatException e) {
            log.error("Create payment failed: Invalid amount format: {}", amount);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Invalid amount format", null));
        }

        // Validate currency
        if (currency == null || currency.trim().isEmpty()) {
            log.error("Create payment failed: Currency is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Currency is required", null));
        }

        try {
            Payment payment = paymentService.createPayment(
                    orderId,
                    parsedAmount,
                    currency,
                    method,
                    description);

            log.info("Payment created successfully - Payment ID: {}, Order ID: {}, Amount: {} {}",
                    payment.getId(), orderId, parsedAmount, currency);

            return ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", "Payment processed successfully", payment));

        } catch (OrderNotFoundException e) {
            log.warn("Create payment failed: Order not found - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (PaymentException e) {
            log.error("Create payment failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while creating payment for order ID: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while processing payment", null));
        }
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
