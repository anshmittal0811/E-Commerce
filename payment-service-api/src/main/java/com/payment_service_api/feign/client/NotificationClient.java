package com.payment_service_api.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.payment_service_api.dto.PaymentRequest;

/**
 * Feign client for communicating with the Notification Service API.
 * 
 * <p>This client provides methods to send notifications via:
 * <ul>
 *   <li>Email - for payment confirmation emails</li>
 *   <li>SMS - for payment confirmation text messages</li>
 * </ul>
 * 
 * <p>Uses service discovery via Eureka to locate the notification-service-api.
 * 
 * <p>Note: This client may be deprecated in favor of Kafka-based
 * notifications for better reliability and async processing.
 */
@FeignClient(name = "notification-service-api")
public interface NotificationClient {

    /**
     * Sends a payment confirmation email.
     * 
     * @param payRequest the payment details for the email
     */
    @PostMapping("/notifications/email")
    void sendEmail(@RequestBody PaymentRequest payRequest);

    /**
     * Sends a payment confirmation SMS.
     * 
     * @param phoneNumber the recipient phone number
     * @param message the SMS message content
     */
    @PostMapping("/notifications/sms")
    void sendSms(@RequestParam("phoneNumber") String phoneNumber, @RequestParam("message") String message);

}
