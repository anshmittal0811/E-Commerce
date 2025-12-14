package com.auth_service_api.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.auth_service_api.filter.HeaderAuthenticationFilter;

/**
 * Spring Security configuration class.
 * 
 * <p>This configuration class sets up:
 * <ul>
 *   <li>Security filter chain with authentication requirements</li>
 *   <li>Public endpoints for authentication (registration and login)</li>
 *   <li>Custom header-based authentication filter</li>
 *   <li>Password encoder bean for password hashing</li>
 * </ul>
 * 
 * <p>Security features:
 * <ul>
 *   <li>CSRF protection is disabled (appropriate for stateless API with JWT)</li>
 *   <li>Method-level security is enabled via {@code @PreAuthorize} annotations</li>
 *   <li>Custom authentication filter extracts user info from gateway-validated headers</li>
 * </ul>
 * 
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private static final String AUTH_ENDPOINTS = "/auth/**";
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
     * Configures the Spring Security filter chain.
     * 
     * <p>This method sets up:
     * <ul>
     *   <li>CSRF protection disabled (stateless API with JWT tokens)</li>
     *   <li>Public access to authentication endpoints ({@code /auth/**})</li>
     *   <li>Public access to Swagger/OpenAPI documentation endpoints</li>
     *   <li>Authentication required for all other endpoints</li>
     *   <li>Custom header authentication filter added before default authentication filter</li>
     * </ul>
     * 
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.debug("Configuring Spring Security filter chain");

        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(AUTH_ENDPOINTS).permitAll()
                    .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                    .anyRequest().authenticated()
            );

        // Add custom header authentication filter before default authentication filter
        // This allows the filter to extract authentication info from gateway headers
        http.addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        SecurityFilterChain filterChain = http.build();
        
        log.info("Spring Security filter chain configured successfully");
        log.debug("Public endpoints: {}", AUTH_ENDPOINTS);
        log.debug("All other endpoints require authentication");

        return filterChain;
    }

    /**
     * Creates a BCrypt password encoder bean for hashing user passwords.
     * 
     * <p>BCrypt is a strong, adaptive hashing algorithm that:
     * <ul>
     *   <li>Automatically handles salt generation</li>
     *   <li>Uses a configurable cost factor (default: 10)</li>
     *   <li>Is resistant to rainbow table attacks</li>
     * </ul>
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("Creating BCrypt password encoder bean");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        log.info("BCrypt password encoder bean created successfully");
        return encoder;
    }
}