package com.gateway_service_api.configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * Configuration for aggregating Swagger documentation from all microservices.
 * 
 * <p>This configuration creates routes that proxy requests to each service's
 * OpenAPI documentation endpoints, allowing the gateway to serve as a central
 * documentation hub.
 */
@Configuration
public class SwaggerAggregationConfig {

    @Bean
    public RouteLocator swaggerRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Auth Service API Docs
            .route("auth-api-docs", r -> r
                .path("/v3/api-docs/auth")
                .and()
                .method(HttpMethod.GET)
                .filters(f -> f.rewritePath("/v3/api-docs/auth", "/v3/api-docs"))
                .uri("lb://AUTH-SERVICE-API"))

            // Product Service API Docs
            .route("product-api-docs", r -> r
                .path("/v3/api-docs/product")
                .and()
                .method(HttpMethod.GET)
                .filters(f -> f.rewritePath("/v3/api-docs/product", "/v3/api-docs"))
                .uri("lb://PRODUCT-SERVICE-API"))

            // Shopping Service API Docs
            .route("shopping-api-docs", r -> r
                .path("/v3/api-docs/shopping")
                .and()
                .method(HttpMethod.GET)
                .filters(f -> f.rewritePath("/v3/api-docs/shopping", "/v3/api-docs"))
                .uri("lb://SHOPPING-SERVICE-API"))

            // Order Service API Docs
            .route("order-api-docs", r -> r
                .path("/v3/api-docs/order")
                .and()
                .method(HttpMethod.GET)
                .filters(f -> f.rewritePath("/v3/api-docs/order", "/v3/api-docs"))
                .uri("lb://ORDER-SERVICE-API"))

            // Payment Service API Docs
            .route("payment-api-docs", r -> r
                .path("/v3/api-docs/payment")
                .and()
                .method(HttpMethod.GET)
                .filters(f -> f.rewritePath("/v3/api-docs/payment", "/v3/api-docs"))
                .uri("lb://PAYMENT-SERVICE-API"))

            // Notification Service API Docs
            .route("notification-api-docs", r -> r
                .path("/v3/api-docs/notification")
                .and()
                .method(HttpMethod.GET)
                .filters(f -> f.rewritePath("/v3/api-docs/notification", "/v3/api-docs"))
                .uri("lb://NOTIFICATION-SERVICE-API"))

            .build();
    }
}

