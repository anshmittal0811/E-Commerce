package com.notification_service_api.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for the Notification Service API.
 * 
 * <p>This configuration provides comprehensive API documentation including:
 * <ul>
 *   <li>API metadata (title, description, version)</li>
 *   <li>Server configuration</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8086}")
    private String serverPort;

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Notification Service API")
                .description("Notification Service for E-Commerce Platform. " +
                    "Handles email notifications, SMS via Twilio, and Kafka message consumption for payment events.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("E-Commerce Team")
                    .email("support@ecommerce.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Local Development Server"),
                new Server()
                    .url("http://gateway-service-api:8090/api/notifications")
                    .description("Gateway Server")));
    }
}

