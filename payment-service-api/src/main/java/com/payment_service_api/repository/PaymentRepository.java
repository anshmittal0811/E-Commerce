package com.payment_service_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payment_service_api.entity.Payment;

/**
 * Repository interface for Payment entity database operations.
 * 
 * <p>This interface extends JpaRepository to provide standard CRUD operations
 * for Payment entities. Spring Data JPA automatically provides implementations
 * for all methods.
 * 
 * <p>Available operations include:
 * <ul>
 *   <li>save - persist a payment record</li>
 *   <li>findById - retrieve a payment by ID</li>
 *   <li>findAll - retrieve all payments</li>
 *   <li>deleteById - remove a payment record</li>
 * </ul>
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
