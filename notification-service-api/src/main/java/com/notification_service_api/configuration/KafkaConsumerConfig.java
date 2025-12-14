package com.notification_service_api.configuration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration for notification processing.
 * 
 * <p>This configuration class sets up the Kafka consumer with:
 * <ul>
 *   <li>JSON deserialization for payment notification messages</li>
 *   <li>Consumer group configuration for message distribution</li>
 *   <li>Offset reset policy for new consumers</li>
 * </ul>
 * 
 * <p>The consumer listens to payment notification messages sent by
 * the Payment Service and triggers email/SMS notifications.
 */
@Configuration
public class KafkaConsumerConfig {

    /**
     * The Kafka bootstrap servers address.
     * Configured via application.yml property.
     */
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Creates the Kafka consumer factory with custom configuration.
     * 
     * <p>Configuration includes:
     * <ul>
     *   <li>String deserializer for message keys</li>
     *   <li>JSON deserializer for message values</li>
     *   <li>Consumer group ID for load balancing</li>
     *   <li>Trust all packages for deserialization</li>
     *   <li>Earliest offset reset for new consumers</li>
     * </ul>
     * 
     * @return the configured ConsumerFactory
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Kafka broker connection
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Consumer group configuration
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service-group");

        // Deserialization configuration
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // JSON deserialization settings
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        // Offset configuration - start from earliest for new consumers
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Creates the Kafka listener container factory.
     * 
     * <p>This factory is used by @KafkaListener annotations to
     * create message listener containers.
     * 
     * @return the configured ConcurrentKafkaListenerContainerFactory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

}
