package com.payment_service_api.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.payment_service_api.filter.HeaderAuthenticationFilter;

/**
 * Security configuration for the Payment Service API.
 * 
 * <p>This configuration class sets up Spring Security with:
 * <ul>
 *   <li>CSRF protection disabled (for REST API usage)</li>
 *   <li>Header-based authentication via custom filter</li>
 *   <li>Method-level security enabled for role-based access control</li>
 * </ul>
 * 
 * <p>All requests require authentication. User identity is extracted
 * from HTTP headers set by the API Gateway.
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

    /**
     * Custom authentication filter that extracts user credentials from HTTP headers.
     */
    private final HeaderAuthenticationFilter headerAuthenticationFilter;

    /**
     * Configures the security filter chain for HTTP requests.
     * 
     * <p>This configuration:
     * <ul>
     *   <li>Disables CSRF protection for stateless REST API</li>
     *   <li>Allows public access to Swagger/OpenAPI documentation</li>
     *   <li>Requires authentication for all other requests</li>
     *   <li>Adds custom header authentication filter before username/password filter</li>
     * </ul>
     * 
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
            );

        http.addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
