package com.shopping_service_api.dto;

import lombok.*;

/**
 * Data Transfer Object for user information received from Auth Service.
 * 
 * <p>Contains user profile details needed for cart association
 * and order processing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    /**
     * The unique identifier of the user.
     */
    private Long id;

    /**
     * The first name of the user.
     */
    private String name;

    /**
     * The last name of the user.
     */
    private String lastName;

    /**
     * The email address of the user.
     */
    private String email;

    /**
     * The role of the user (e.g., ADMIN, CLIENT).
     */
    private String role;

    /**
     * The shipping address of the user.
     */
    private String address;

    /**
     * The phone number of the user.
     */
    private String phone;

}
