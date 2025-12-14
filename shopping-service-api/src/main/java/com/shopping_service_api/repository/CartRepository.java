package com.shopping_service_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopping_service_api.entity.Cart;

/**
 * Repository interface for Cart entity database operations.
 * 
 * <p>This interface extends JpaRepository to provide standard CRUD operations
 * for Cart entities. Spring Data JPA automatically provides implementations
 * for all methods.
 * 
 * <p>Custom query methods:
 * <ul>
 *   <li>{@link #findByIdUser(Long)} - Find cart by user ID</li>
 * </ul>
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Finds a cart by the associated user ID.
     * 
     * @param idUser the user ID to search for
     * @return the cart if found, null otherwise
     */
    Cart findByIdUser(Long idUser);

}
