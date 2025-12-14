package com.gateway_service_api.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for the Gateway Service.
 * 
 * <p>This configuration provides the main API documentation hub that aggregates
 * documentation from all microservices in the e-commerce platform.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("E-Commerce Platform API Gateway")
                .description("Central API Gateway for the E-Commerce Microservices Platform. " +
                    "This gateway provides a unified entry point to all services including: " +
                    "Auth, Product, Shopping, Order, Payment, and Notification services. " +
                    "Use the dropdown above to switch between different service APIs.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("E-Commerce Team")
                    .email("support@ecommerce.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}

