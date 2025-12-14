package com.shopping_service_api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.shopping_service_api.dto.ApiResponse;
import com.shopping_service_api.dto.ProductResponse;

/**
 * Feign client for communicating with the Product Service API.
 * 
 * <p>This client provides methods to:
 * <ul>
 *   <li>Retrieve product information by ID</li>
 *   <li>Update product stock after cart operations</li>
 * </ul>
 * 
 * <p>Uses service discovery via Eureka to locate the product-service-api.
 * 
 * <p>Note: All responses are wrapped in ApiResponse as per the standard
 * response format used by internal microservices.
 */
@FeignClient(name = "product-service-api")
public interface ProductServiceClient {

    /**
     * Retrieves a product by its unique identifier.
     * 
     * @param idProduct the product ID to retrieve
     * @return the API response containing product details
     */
    @GetMapping("/product/{idProduct}")
    ApiResponse<ProductResponse> findProductById(@PathVariable("idProduct") Long idProduct);

    /**
     * Updates the stock quantity for a specific product.
     * 
     * @param idProduct the product ID to update
     * @param quantity the quantity to deduct from stock
     * @return the API response containing updated product details
     */
    @PutMapping("/product/update/stock/{idProduct}")
    ApiResponse<ProductResponse> updateStockProduct(@PathVariable("idProduct") Long idProduct, @RequestBody Integer quantity);

}
