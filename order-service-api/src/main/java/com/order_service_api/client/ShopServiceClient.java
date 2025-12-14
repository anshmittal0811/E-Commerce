package com.order_service_api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.order_service_api.dto.ApiResponse;
import com.order_service_api.dto.CartResponse;

/**
 * Feign client for communicating with the Shopping Service API.
 * 
 * <p>This client provides methods to interact with the user's shopping cart
 * in the shopping-service. It uses service discovery to locate the shopping-service-api instance.
 * 
 * <p>Authentication headers are automatically forwarded via {@link com.order_service_api.interceptor.FeignAuthInterceptor}.
 * 
 * <p>Note: All responses are wrapped in ApiResponse as per the standard
 * response format used by internal microservices.
 * 
 * @see CartResponse
 */
@FeignClient(name = "shopping-service-api")
public interface ShopServiceClient {

    /**
     * Retrieves the current user's shopping cart.
     * 
     * <p>This method fetches the cart contents including all items
     * and the calculated total for order creation.
     * 
     * @return ApiResponse containing CartResponse with cart items and total
     */
    @GetMapping("/shopping/send-cart")
    ApiResponse<CartResponse> sendCart();

    /**
     * Clears the current user's shopping cart.
     * 
     * <p>This method is called after successful order creation
     * to empty the cart.
     * 
     * @return ApiResponse with success/error status
     */
    @GetMapping("/shopping/clear-cart")
    ApiResponse<Void> cleanCart();

}
