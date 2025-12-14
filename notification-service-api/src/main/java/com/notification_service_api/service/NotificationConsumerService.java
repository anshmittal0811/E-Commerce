package com.notification_service_api.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.notification_service_api.dto.PaymentNotificationMessage;
import com.notification_service_api.dto.PaymentResponse;
import com.notification_service_api.dto.PaymentResponse.StatusOrder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Kafka consumer service for processing payment notifications.
 * 
 * <p>This service:
 * <ul>
 *   <li>Listens to the 'payment-notifications' Kafka topic</li>
 *   <li>Converts incoming messages to PaymentResponse DTOs</li>
 *   <li>Triggers email notifications via EmailService</li>
 * </ul>
 * 
 * <p>Messages are consumed asynchronously and processed independently.
 * Failures are logged but do not affect other message processing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumerService {

    private final EmailService emailService;
    private final SmsService smsService;

    /**
     * Consumes payment notification messages from Kafka.
     * 
     * <p>This method is triggered automatically when a message is
     * published to the 'payment-notifications' topic. It:
     * <ul>
     *   <li>Logs the incoming message</li>
     *   <li>Converts the message to a PaymentResponse</li>
     *   <li>Sends an email notification</li>
     *   <li>Optionally sends an SMS notification</li>
     * </ul>
     * 
     * @param message the payment notification message from Kafka
     */
    @KafkaListener(topics = "payment-notifications", groupId = "notification-service-group")
    public void consumePaymentNotification(PaymentNotificationMessage message) {
        log.info("Received payment notification - Order ID: {}, Email: {}",
                message.getOrderId(), message.getUserEmail());

        // Validate message
        if (message.getOrderId() == null) {
            log.error("Invalid payment notification: Order ID is null");
            return;
        }

        try {
            // Convert message to PaymentResponse
            PaymentResponse paymentResponse = buildPaymentResponse(message);

            // Send email notification
            sendEmailNotification(paymentResponse);

            // Optionally send SMS notification
            sendSmsNotification(message);

            log.info("Payment notification processed successfully - Order ID: {}", message.getOrderId());

        } catch (Exception e) {
            log.error("Error processing payment notification - Order ID: {}, Error: {}",
                    message.getOrderId(), e.getMessage(), e);
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * Builds a PaymentResponse from the Kafka message.
     * 
     * @param message the incoming Kafka message
     * @return the populated PaymentResponse
     */
    private PaymentResponse buildPaymentResponse(PaymentNotificationMessage message) {
        return PaymentResponse.builder()
                .orderId(message.getOrderId())
                .userName(message.getUserName())
                .userEmail(message.getUserEmail())
                .userAddress(message.getUserAddress())
                .userPhone(message.getUserPhone())
                .orderStatus(mapStatusOrder(message.getOrderStatus()))
                .orderDate(message.getOrderDate() != null ? message.getOrderDate() : LocalDateTime.now())
                .totalAmount(message.getTotalAmount())
                .build();
    }

    /**
     * Sends an email notification for the payment.
     * 
     * @param paymentResponse the payment data
     */
    private void sendEmailNotification(PaymentResponse paymentResponse) {
        try {
            emailService.sendEmail(paymentResponse);
            log.info("Email notification sent - Order ID: {}, Email: {}",
                    paymentResponse.getOrderId(), paymentResponse.getUserEmail());

        } catch (Exception e) {
            log.error("Failed to send email notification - Order ID: {}, Error: {}",
                    paymentResponse.getOrderId(), e.getMessage());
            // Don't rethrow - continue with other notifications
        }
    }

    /**
     * Sends an SMS notification for the payment if phone number is available.
     * 
     * @param message the payment notification message
     */
    private void sendSmsNotification(PaymentNotificationMessage message) {
        if (message.getUserPhone() == null || message.getUserPhone().isBlank()) {
            log.debug("Skipping SMS notification - No phone number for order ID: {}", message.getOrderId());
            return;
        }

        try {
            smsService.sendOrderConfirmationSms(
                    message.getUserPhone(),
                    message.getOrderId(),
                    message.getTotalAmount());

            log.info("SMS notification sent - Order ID: {}, Phone: {}",
                    message.getOrderId(), message.getUserPhone());

        } catch (Exception e) {
            log.error("Failed to send SMS notification - Order ID: {}, Error: {}",
                    message.getOrderId(), e.getMessage());
            // Don't rethrow - SMS is optional
        }
    }

    /**
     * Maps a string status to the StatusOrder enum.
     * 
     * <p>Handles various formats including fully qualified enum names
     * and plain status values.
     * 
     * @param status the status string to map
     * @return the corresponding StatusOrder enum value
     */
    private StatusOrder mapStatusOrder(String status) {
        if (status == null) {
            log.debug("Order status is null, defaulting to COMPLETED");
            return StatusOrder.COMPLETED;
        }

        try {
            // Handle enum string format (e.g., "COMPLETED" or "com.payment_service_api.dto.OrderResponse$StatusOrder:COMPLETED")
            String statusValue = status;
            if (status.contains(":")) {
                statusValue = status.substring(status.lastIndexOf(':') + 1);
            }
            return StatusOrder.valueOf(statusValue.toUpperCase());

        } catch (IllegalArgumentException e) {
            log.warn("Unknown order status '{}', defaulting to COMPLETED", status);
            return StatusOrder.COMPLETED;
        }
    }
}
