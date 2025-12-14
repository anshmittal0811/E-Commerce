package com.payment_service_api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payment_service_api.dto.OrderResponse;
import com.payment_service_api.dto.PaymentRequest;
import com.payment_service_api.entity.Payment;
import com.payment_service_api.exception.OrderNotFoundException;
import com.payment_service_api.exception.PaymentException;
import com.payment_service_api.feign.client.OrderServiceClient;
import com.payment_service_api.repository.PaymentRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Service class for payment processing operations.
 * 
 * <p>This service handles:
 * <ul>
 *   <li>Viewing order details before payment</li>
 *   <li>Creating and processing payments</li>
 *   <li>Updating order status after payment</li>
 *   <li>Triggering payment notifications via Kafka</li>
 * </ul>
 * 
 * <p>Integrates with Order Service via Feign client and
 * Notification Service via Kafka producer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final OrderServiceClient orderServiceClient;
    private final PaymentRepository paymentRepository;
    private final NotificationProducerService notificationProducerService;

    /**
     * Retrieves order details for payment display.
     * 
     * @param orderId the order ID to retrieve
     * @return the order details
     * @throws OrderNotFoundException if the order does not exist
     */
    @Transactional(readOnly = true)
    public OrderResponse viewOrderDetails(Long orderId) {
        log.debug("Fetching order details for order ID: {}", orderId);

        try {
            OrderResponse orderResponse = orderServiceClient.bringOrder(orderId);

            if (orderResponse == null) {
                log.warn("Order not found with ID: {}", orderId);
                throw new OrderNotFoundException(orderId);
            }

            log.debug("Order details retrieved - Order ID: {}, Status: {}, Total: {}",
                    orderId, orderResponse.getOrderStatus(), orderResponse.getTotalAmount());

            return orderResponse;

        } catch (FeignException.NotFound e) {
            log.warn("Order not found via Order Service - Order ID: {}", orderId);
            throw new OrderNotFoundException(orderId);

        } catch (FeignException e) {
            log.error("Error communicating with Order Service for order ID: {}", orderId, e);
            throw new PaymentException("Failed to retrieve order details", e);
        }
    }

    /**
     * Creates and processes a payment for an order.
     * 
     * <p>This method:
     * <ul>
     *   <li>Creates a payment record with SUCCESS status</li>
     *   <li>Updates the order status to COMPLETED via Order Service</li>
     *   <li>Sends a payment notification asynchronously via Kafka</li>
     * </ul>
     * 
     * @param orderId the order ID to pay for
     * @param total the payment amount
     * @param currency the currency code
     * @param method the payment method
     * @param description optional payment description
     * @return the created payment entity
     * @throws OrderNotFoundException if the order does not exist
     * @throws PaymentException if payment processing fails
     */
    @Transactional
    public Payment createPayment(
            Long orderId,
            Double total,
            String currency,
            String method,
            String description) {

        log.info("Creating payment for order ID: {} - Amount: {} {}, Method: {}",
                orderId, total, currency, method);

        // Build payment entity
        Payment payment = Payment.builder()
                .orderId(orderId)
                .total(total)
                .currency(currency)
                .description(description)
                .method(method)
                .status("SUCCESS")
                .build();

        // Complete the order via Order Service
        try {
            orderServiceClient.completeOrder(orderId);
            log.debug("Order status updated to COMPLETED for order ID: {}", orderId);

        } catch (FeignException.NotFound e) {
            log.error("Order not found while completing - Order ID: {}", orderId);
            throw new OrderNotFoundException(orderId);

        } catch (FeignException e) {
            log.error("Failed to complete order via Order Service - Order ID: {}", orderId, e);
            throw new PaymentException("Failed to complete order", e);
        }

        // Save payment to database
        Payment savedPayment;
        try {
            savedPayment = paymentRepository.save(payment);
            log.info("Payment saved successfully - Payment ID: {}, Order ID: {}",
                    savedPayment.getId(), orderId);

        } catch (Exception e) {
            log.error("Failed to save payment for order ID: {}", orderId, e);
            throw new PaymentException("Failed to save payment record", e);
        }

        // Send notification asynchronously via Kafka
        sendPaymentNotification(orderId, savedPayment);

        return savedPayment;
    }

    // ==================== Private Helper Methods ====================

    /**
     * Sends a payment notification asynchronously via Kafka.
     * 
     * <p>This method is fire-and-forget - failures are logged but
     * do not affect the payment transaction.
     * 
     * @param orderId the order ID
     * @param payment the payment entity
     */
    private void sendPaymentNotification(Long orderId, Payment payment) {
        try {
            OrderResponse orderResponse = orderServiceClient.bringOrder(orderId);
            PaymentRequest paymentRequest = buildPaymentRequest(orderResponse, payment);
            notificationProducerService.sendPaymentNotification(paymentRequest);

            log.info("Payment notification sent to Kafka for order ID: {}", orderId);

        } catch (Exception e) {
            // Log error but don't fail the transaction - notification is secondary
            log.error("Failed to send payment notification for order ID: {} - {}",
                    orderId, e.getMessage(), e);
        }
    }

    /**
     * Builds a PaymentRequest DTO from order and payment information.
     * 
     * @param orderResponse the order details
     * @param payment the payment entity
     * @return the populated PaymentRequest
     */
    private PaymentRequest buildPaymentRequest(OrderResponse orderResponse, Payment payment) {
        return PaymentRequest.builder()
                .orderId(orderResponse.getOrderId())
                .userName(orderResponse.getName() + " " + orderResponse.getLastName())
                .userEmail(orderResponse.getEmail())
                .userAddress(orderResponse.getAddress())
                .userPhone(orderResponse.getPhone())
                .orderStatus(orderResponse.getOrderStatus())
                .orderDate(orderResponse.getOrderDate() != null
                        ? orderResponse.getOrderDate()
                        : LocalDateTime.now())
                .totalAmount(payment.getTotal())
                .build();
    }

}
