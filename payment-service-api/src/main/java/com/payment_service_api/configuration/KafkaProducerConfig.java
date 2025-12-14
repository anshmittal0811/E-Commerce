package com.payment_service_api.configuration;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.payment_service_api.dto.PaymentRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka producer configuration for payment notifications.
 * 
 * <p>This configuration class sets up the Kafka producer with:
 * <ul>
 *   <li>JSON serialization for payment request messages</li>
 *   <li>Acknowledgment configuration for reliability</li>
 *   <li>Retry configuration for transient failures</li>
 * </ul>
 * 
 * <p>The producer sends payment notification messages to be consumed
 * by the Notification Service for email and SMS delivery.
 */
@Configuration
public class KafkaProducerConfig {

    /**
     * The Kafka bootstrap servers address.
     * Configured via application.yml property.
     */
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Creates the Kafka producer factory with custom configuration.
     * 
     * <p>Configuration includes:
     * <ul>
     *   <li>String serializer for message keys</li>
     *   <li>JSON serializer for PaymentRequest values</li>
     *   <li>Acknowledgment from leader only (acks=1)</li>
     *   <li>3 retry attempts for failed sends</li>
     * </ul>
     * 
     * @return the configured ProducerFactory
     */
    @Bean
    public ProducerFactory<String, PaymentRequest> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Kafka broker connection
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Serialization configuration
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        // Reliability configuration
        configProps.put(ProducerConfig.ACKS_CONFIG, "1");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Creates the KafkaTemplate for sending messages.
     * 
     * @return the configured KafkaTemplate
     */
    @Bean
    public KafkaTemplate<String, PaymentRequest> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
