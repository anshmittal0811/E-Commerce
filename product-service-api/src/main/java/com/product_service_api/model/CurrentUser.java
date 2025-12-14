package com.product_service_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Model class representing the current authenticated user.
 * 
 * <p>This class holds user identity information extracted from:
 * <ul>
 *   <li>Spring Security Authentication object</li>
 *   <li>HTTP request headers (X-USER-EMAIL, X-USER-ROLE, X-USER-ID)</li>
 * </ul>
 * 
 * <p>Used primarily for authorization and audit logging purposes.
 */
@Data
@AllArgsConstructor
public class CurrentUser {

    /**
     * The email address of the authenticated user.
     */
    private String email;

    /**
     * The role of the authenticated user (e.g., ADMIN, CLIENT).
     */
    private String role;

    /**
     * The unique identifier of the authenticated user.
     */
    private Long userId;

}
