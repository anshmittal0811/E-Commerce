package com.auth_service_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.auth_service_api.dto.LoginRequest;
import com.auth_service_api.dto.RegisterRequest;
import com.auth_service_api.exception.EmailAlreadyExistsException;
import com.auth_service_api.service.AuthService;

/**
 * REST controller for authentication operations.
 * 
 * <p>This controller provides endpoints for:
 * <ul>
 *   <li>User registration</li>
 *   <li>User login and JWT token generation</li>
 * </ul>
 * 
 * <p>All endpoints are publicly accessible (no authentication required).
 * 
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user in the system.
     * 
     * <p>This endpoint accepts user registration information and creates a new user account.
     * The email must be unique in the system.
     * 
     * @param registerRequest the registration request containing user details (name, email, password, role, etc.)
     * @return ResponseEntity containing a success message
     */
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with the provided details. Email must be unique."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Email already exists"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest registerRequest) {
        if (registerRequest == null) {
            log.error("Registration request is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("ERROR", "Registration request cannot be null"));
        }

        log.info("Registration request received for email: {}", registerRequest.getEmail());

        try {
            String message = authService.register(registerRequest);
            
            log.info("User registration successful for email: {}", registerRequest.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("SUCCESS", message));
            
        } catch (EmailAlreadyExistsException e) {
            log.warn("Registration failed: Email already exists - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse("ERROR", e.getMessage()));
            
        } catch (IllegalArgumentException e) {
            log.error("Registration failed: Invalid request data - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("ERROR", e.getMessage()));
            
        } catch (Exception e) {
            log.error("Unexpected error during user registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("ERROR", "An unexpected error occurred during registration"));
        }
    }

    /**
     * Authenticates a user and returns a JWT token.
     * 
     * <p>This endpoint validates user credentials and returns a JWT token upon successful authentication.
     * The token can be used for subsequent authenticated requests.
     * 
     * @param loginRequest the login request containing email and password
     * @return ResponseEntity containing the JWT token and success message
     */
    @Operation(
        summary = "User login",
        description = "Authenticates a user with email and password, returns a JWT token on success"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid credentials"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest == null) {
            log.error("Login request is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse("ERROR", "Login request cannot be null", null));
        }

        log.info("Login request received for email: {}", loginRequest.getEmail());

        try {
            String token = authService.login(loginRequest);
            
            log.info("Login successful for email: {}", loginRequest.getEmail());
            
            return ResponseEntity.ok(new LoginResponse("SUCCESS", "Login successful", token));
            
        } catch (UsernameNotFoundException e) {
            log.warn("Login failed: User not found - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("ERROR", "Invalid email or password", null));
            
        } catch (BadCredentialsException e) {
            log.warn("Login failed: Invalid credentials for email: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("ERROR", "Invalid email or password", null));
            
        } catch (IllegalArgumentException e) {
            log.error("Login failed: Invalid request data - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse("ERROR", e.getMessage(), null));
            
        } catch (Exception e) {
            log.error("Unexpected error during user login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse("ERROR", "An unexpected error occurred during login", null));
        }
    }

    /**
     * Standard API response structure for registration endpoint.
     * 
     * @param status the status of the operation (SUCCESS or ERROR)
     * @param message the response message
     */
    public record ApiResponse(String status, String message) {}

    /**
     * Response structure for login endpoint.
     * 
     * @param status the status of the operation (SUCCESS or ERROR)
     * @param message the response message
     * @param token the JWT token (null if login failed)
     */
    public record LoginResponse(String status, String message, String token) {}
}