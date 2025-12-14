package com.notification_service_api.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class for sending SMS notifications.
 * 
 * <p>This service handles:
 * <ul>
 *   <li>Sending SMS messages to customers</li>
 *   <li>Order confirmation SMS notifications</li>
 * </ul>
 * 
 * <p>Note: Current implementation logs SMS details.
 * For production use, integrate with an SMS provider
 * such as Twilio, AWS SNS, or similar.
 */
@Service
@Slf4j
public class SmsService {

    /**
     * Sends an SMS message to the specified phone number.
     * 
     * <p>Currently logs the SMS details. In production, this should
     * be integrated with an SMS gateway provider.
     * 
     * @param toPhoneNumber the recipient phone number
     * @param messageBody the SMS message content
     * @throws IllegalArgumentException if phone number or message is invalid
     */
    public void sendSms(String toPhoneNumber, String messageBody) {
        // Validate inputs
        if (toPhoneNumber == null || toPhoneNumber.isBlank()) {
            log.error("SMS send failed: Phone number is required");
            throw new IllegalArgumentException("Phone number cannot be null or blank");
        }
        if (messageBody == null || messageBody.isBlank()) {
            log.error("SMS send failed: Message body is required");
            throw new IllegalArgumentException("Message body cannot be null or blank");
        }

        log.info("Sending SMS - To: {}, Message length: {} chars", toPhoneNumber, messageBody.length());

        // TODO: Integrate with SMS provider (e.g., Twilio, AWS SNS)
        // For now, just log the SMS details
        log.info("SMS sent successfully - To: {}, Message: {}", toPhoneNumber, messageBody);
    }

    /**
     * Sends an order confirmation SMS to the customer.
     * 
     * @param toPhoneNumber the customer phone number
     * @param orderId the order ID
     * @param totalAmount the order total amount
     */
    public void sendOrderConfirmationSms(String toPhoneNumber, Long orderId, Double totalAmount) {
        log.debug("Preparing order confirmation SMS - Order ID: {}, Phone: {}", orderId, toPhoneNumber);

        String message = String.format(
                "Your order #%d has been confirmed! Total: $%.2f. Thank you for shopping with us.",
                orderId,
                totalAmount != null ? totalAmount : 0.0);

        sendSms(toPhoneNumber, message);
    }

}
