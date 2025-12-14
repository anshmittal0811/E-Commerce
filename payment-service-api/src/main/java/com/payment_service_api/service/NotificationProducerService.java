package com.payment_service_api.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.payment_service_api.dto.PaymentRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * Service class for producing payment notification messages to Kafka.
 * 
 * <p>This service is responsible for:
 * <ul>
 *   <li>Sending payment notification messages to the Kafka topic</li>
 *   <li>Handling async message delivery with callbacks</li>
 *   <li>Logging message delivery success and failures</li>
 * </ul>
 * 
 * <p>Messages are sent asynchronously and delivery status is logged
 * via completion callbacks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducerService {

    /**
     * The Kafka topic for payment notifications.
     */
    private static final String TOPIC = "payment-notifications";

    private final KafkaTemplate<String, PaymentRequest> kafkaTemplate;

    /**
     * Sends a payment notification message to Kafka asynchronously.
     * 
     * <p>This method uses a CompletableFuture to handle the async response
     * and logs the result. Failures are logged but do not throw exceptions
     * to prevent affecting the calling transaction.
     * 
     * @param paymentRequest the payment notification data to send
     */
    public void sendPaymentNotification(PaymentRequest paymentRequest) {
        log.debug("Sending payment notification to Kafka - Order ID: {}, Email: {}",
                paymentRequest.getOrderId(), paymentRequest.getUserEmail());

        try {
            CompletableFuture<SendResult<String, PaymentRequest>> future =
                    kafkaTemplate.send(TOPIC, paymentRequest);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Payment notification sent successfully - Order ID: {}, Offset: {}",
                            paymentRequest.getOrderId(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send payment notification - Order ID: {}, Error: {}",
                            paymentRequest.getOrderId(),
                            ex.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("Error sending payment notification to Kafka - Order ID: {}, Error: {}",
                    paymentRequest.getOrderId(), e.getMessage(), e);
        }
    }

}
