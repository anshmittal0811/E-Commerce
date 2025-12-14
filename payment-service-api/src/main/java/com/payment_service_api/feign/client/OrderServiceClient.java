package com.payment_service_api.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.payment_service_api.dto.ApiResponse;
import com.payment_service_api.dto.OrderResponse;

/**
 * Feign client for communicating with the Order Service API.
 * 
 * <p>This client provides methods to:
 * <ul>
 *   <li>Retrieve order details for payment processing</li>
 *   <li>Update order status to COMPLETED after payment</li>
 * </ul>
 * 
 * <p>Uses service discovery via Eureka to locate the order-service-api.
 * 
 * <p>Note: All responses are wrapped in ApiResponse as per the standard
 * response format used by internal microservices.
 */
@FeignClient(name = "order-service-api")
public interface OrderServiceClient {

    /**
     * Retrieves order details by order ID.
     * 
     * <p>Also updates the order status to PROCESSING.
     * 
     * @param orderId the order ID to retrieve
     * @return the API response containing order details
     */
    @GetMapping("/orders/bring/{orderId}")
    ApiResponse<OrderResponse> bringOrder(@PathVariable Long orderId);

    /**
     * Marks an order as completed after successful payment.
     * 
     * @param orderId the order ID to complete
     * @return the API response containing updated order details
     */
    @PostMapping("/orders/complete/{orderId}")
    ApiResponse<OrderResponse> completeOrder(@PathVariable Long orderId);

}
