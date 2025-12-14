package com.product_service_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.product_service_api.entity.Product;

/**
 * Repository interface for Product entity database operations.
 * 
 * <p>This interface extends JpaRepository to provide standard CRUD operations
 * for Product entities. Spring Data JPA automatically provides implementations
 * for all methods.
 * 
 * <p>Available operations include:
 * <ul>
 *   <li>save, saveAll - persist product(s)</li>
 *   <li>findById, findAll - retrieve product(s)</li>
 *   <li>deleteById, delete - remove product(s)</li>
 *   <li>existsById - check product existence</li>
 * </ul>
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

}
