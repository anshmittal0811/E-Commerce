package com.notification_service_api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

/**
 * Kafka consumer configuration for notification processing.
 * 
 * <p>This configuration enables Kafka listener functionality.
 * Consumer properties are defined in application.yml:
 * <ul>
 *   <li>bootstrap-servers - Kafka broker address</li>
 *   <li>group-id - Consumer group for load balancing</li>
 *   <li>auto-offset-reset - Offset strategy for new consumers</li>
 *   <li>key/value deserializers - Message deserialization</li>
 *   <li>trusted.packages - Allowed packages for JSON deserialization</li>
 *   <li>use.type.headers - Disabled to match producer settings</li>
 * </ul>
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    /**
     * Creates the Kafka listener container factory.
     * 
     * <p>This factory is used by @KafkaListener annotations to
     * create message listener containers. Uses Spring Boot's
     * auto-configured ConsumerFactory based on application.yml properties.
     * 
     * @param consumerFactory the auto-configured consumer factory from Spring Boot
     * @return the configured ConcurrentKafkaListenerContainerFactory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

}
