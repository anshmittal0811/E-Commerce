package com.order_service_api.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.order_service_api.filter.HeaderAuthenticationFilter;

/**
 * Security configuration for the Order Service API.
 * 
 * <p>This configuration:
 * <ul>
 *   <li>Disables CSRF protection (API uses stateless authentication)</li>
 *   <li>Requires authentication for all requests</li>
 *   <li>Enables method-level security annotations (@PreAuthorize)</li>
 *   <li>Adds custom header-based authentication filter</li>
 * </ul>
 * 
 * <p>Authentication is performed via the {@link HeaderAuthenticationFilter} which
 * extracts user identity from HTTP headers set by the API Gateway.
 * 
 * @see HeaderAuthenticationFilter
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] SWAGGER_ENDPOINTS = {
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/v3/api-docs.yaml",
        "/swagger-resources/**",
        "/webjars/**"
    };

    private final HeaderAuthenticationFilter headerAuthenticationFilter;

    /**
     * Configures the security filter chain.
     * 
     * <p>This method sets up:
     * <ul>
     *   <li>CSRF disabled (stateless API)</li>
     *   <li>Public access to Swagger/OpenAPI documentation endpoints</li>
     *   <li>All other requests require authentication</li>
     *   <li>Header authentication filter before username/password filter</li>
     * </ul>
     * 
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API
            .csrf(AbstractHttpConfigurer::disable)
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
            );

        // Add custom header authentication filter
        http.addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
