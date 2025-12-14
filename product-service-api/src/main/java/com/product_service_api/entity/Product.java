package com.product_service_api.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity class representing a product in the catalog.
 * 
 * <p>This entity maps to the 'products' table and contains
 * all product-related information including name, description,
 * price, category, and stock quantity.
 * 
 * <p>Product names must be unique across the catalog.
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /**
     * Unique identifier for the product.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the product.
     * Must be unique and non-null.
     */
    @Column(columnDefinition = "TEXT", nullable = false, unique = true)
    private String name;

    /**
     * Detailed description of the product.
     * Optional field.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Price of the product.
     * Must be a non-null positive value.
     */
    @Column(nullable = false)
    private Double price;

    /**
     * Category classification of the product.
     * Stored as a string in the database.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    /**
     * Current stock quantity available.
     * Must be a non-null non-negative value.
     */
    @Column(nullable = false)
    private Integer stock;

    /**
     * Enumeration of product categories.
     * 
     * <p>Categories represent different quality/tier levels:
     * <ul>
     *   <li>AAA - Premium tier</li>
     *   <li>AA - Standard tier</li>
     *   <li>A - Basic tier</li>
     * </ul>
     */
    public enum Category {
        /** Premium tier products */
        AAA,
        /** Standard tier products */
        AA,
        /** Basic tier products */
        A
    }

}
