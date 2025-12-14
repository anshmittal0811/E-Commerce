package com.auth_service_api.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.auth_service_api.entity.User;
import com.auth_service_api.exception.UserNotFoundException;
import com.auth_service_api.service.UserService;

/**
 * REST controller for user management operations.
 * 
 * <p>This controller provides endpoints for:
 * <ul>
 *   <li>Retrieving user information (admin and client access)</li>
 *   <li>Role verification endpoints for authorization testing</li>
 * </ul>
 * 
 * <p>All endpoints are secured with method-level security annotations.
 * 
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Retrieves all users from the system.
     * 
     * <p>This endpoint is restricted to ADMIN role only. It returns a list of all
     * registered users in the system.
     * 
     * @return ResponseEntity containing a list of all users, or an empty list if no users exist
     */
    @Operation(summary = "Get all users", description = "Retrieves all users from the system (Admin only)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Admin requesting all users");

        try {
            List<User> users = userService.getAllUsers();
            
            log.info("Successfully retrieved {} user(s) for admin", users.size());
            
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            log.error("Error occurred while retrieving all users for admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a specific user by their unique identifier.
     * 
     * <p>This endpoint allows clients to retrieve their own user information.
     * The user ID is extracted from the path variable.
     * 
     * @param id the unique identifier of the user to retrieve
     * @return ResponseEntity containing the user if found
     * @throws UserNotFoundException if no user exists with the provided ID
     */
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/client/user/{id}")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("Client requesting user with ID: {}", id);

        try {
            User user = userService.getUserById(id);
            
            log.info("Successfully retrieved user with ID: {}, email: {}", 
                    user.getId(), user.getEmail());
            
            return ResponseEntity.ok(user);
            
        } catch (UserNotFoundException e) {
            log.warn("User not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID provided: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            
        } catch (Exception e) {
            log.error("Error occurred while retrieving user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verifies that the current authenticated user has ADMIN role.
     * 
     * <p>This endpoint is used for authorization testing. It returns true if the
     * authenticated user has ADMIN authority, false otherwise.
     * 
     * @return true if the user has ADMIN role, false otherwise
     */
    @Operation(summary = "Verify admin role", description = "Verifies that the current user has ADMIN role")
    @GetMapping("/admin/verification")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> isAdmin() {
        log.debug("Admin role verification requested");
        return ResponseEntity.ok(true);
    }

    /**
     * Verifies that the current authenticated user has CLIENT role.
     * 
     * <p>This endpoint is used for authorization testing. It returns true if the
     * authenticated user has CLIENT authority, false otherwise.
     * 
     * @return true if the user has CLIENT role, false otherwise
     */
    @Operation(summary = "Verify client role", description = "Verifies that the current user has CLIENT role")
    @GetMapping("/client/verification")
    @PreAuthorize("hasAuthority('CLIENT')")
    public ResponseEntity<Boolean> isClient() {
        log.debug("Client role verification requested");
        return ResponseEntity.ok(true);
    }

    /**
     * Verifies that the current authenticated user has either CLIENT or ADMIN role.
     * 
     * <p>This endpoint is used for authorization testing. It returns true if the
     * authenticated user has either CLIENT or ADMIN authority, false otherwise.
     * 
     * @return true if the user has CLIENT or ADMIN role, false otherwise
     */
    @Operation(summary = "Verify user role", description = "Verifies that the current user has CLIENT or ADMIN role")
    @GetMapping("/user/verification")
    @PreAuthorize("hasAnyAuthority('CLIENT','ADMIN')")
    public ResponseEntity<Boolean> isUser() {
        log.debug("User role verification requested (CLIENT or ADMIN)");
        return ResponseEntity.ok(true);
    }
}