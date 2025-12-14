package com.product_service_api.controller;

import java.util.List;

import com.product_service_api.exception.ProductNotFoundException;
import com.product_service_api.exception.ProductOperationException;
import com.product_service_api.model.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.product_service_api.entity.Product;
import com.product_service_api.service.ProductService;

/**
 * REST controller for product management operations.
 * 
 * <p>This controller provides endpoints for:
 * <ul>
 *   <li>Retrieving all products (CLIENT access)</li>
 *   <li>Retrieving a product by ID (public access)</li>
 *   <li>Updating product stock (CLIENT access)</li>
 *   <li>Saving new products (ADMIN access)</li>
 *   <li>Bulk saving products (ADMIN access)</li>
 * </ul>
 * 
 * <p>All endpoints except findProductById require authentication. 
 * Admin-only endpoints are protected with @PreAuthorize annotations.
 */
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product catalog management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    /**
     * Retrieves all available products from the catalog.
     * 
     * <p>This endpoint is restricted to CLIENT users.
     * 
     * @return ResponseEntity containing the list of all products or error message
     */
    @Operation(summary = "Get all products", description = "Retrieves all available products from the catalog")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('CLIENT')")
    public ResponseEntity<ApiResponse<List<Product>>> findAllProducts() {
        log.info("Get all products request received");

        try {
            List<Product> products = productService.findAllProducts();

            if (products.isEmpty()) {
                log.debug("No products found in catalog");
                return ResponseEntity.ok(
                        new ApiResponse<>("SUCCESS", "No products found", products));
            }

            log.info("Successfully retrieved {} product(s)", products.size());

            return ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", "Products retrieved successfully", products));

        } catch (Exception e) {
            log.error("Unexpected error while retrieving all products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while retrieving products", null));
        }
    }

    /**
     * Updates the stock quantity for a specific product.
     * 
     * <p>This endpoint decreases the stock by the specified amount.
     * It is restricted to CLIENT users for order processing.
     * 
     * @param idProduct the unique identifier of the product to update
     * @param quantity the quantity to reduce from stock
     * @return ResponseEntity containing the updated product or error message
     */
    @Operation(summary = "Update product stock", description = "Updates the stock quantity for a specific product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PutMapping("/update/stock/{idProduct}")
    @PreAuthorize("hasAuthority('CLIENT')")
    public ResponseEntity<ApiResponse<Product>> updateStockProduct(
            @Parameter(description = "Product ID") @PathVariable("idProduct") Long idProduct,
            @RequestBody Integer quantity) {

        log.info("Update stock request received for product ID: {} with quantity: {}", idProduct, quantity);

        // Validate input parameters
        if (idProduct == null || idProduct <= 0) {
            log.error("Update stock failed: Invalid product ID: {}", idProduct);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Invalid product ID", null));
        }

        if (quantity == null || quantity < 0) {
            log.error("Update stock failed: Invalid quantity: {}", quantity);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Invalid quantity value", null));
        }

        try {
            Product updatedProduct = productService.updateStockProduct(idProduct, quantity);

            log.info("Stock updated successfully for product ID: {}. New stock: {}",
                    idProduct, updatedProduct.getStock());

            return ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", "Stock updated successfully", updatedProduct));

        } catch (ProductNotFoundException e) {
            log.warn("Product not found with ID: {}", idProduct);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (ProductOperationException e) {
            log.error("Stock update failed for product ID: {} - {}", idProduct, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while updating stock for product ID: {}", idProduct, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while updating stock", null));
        }
    }

    /**
     * Saves a new product to the catalog.
     * 
     * <p>This endpoint is restricted to ADMIN users only.
     * 
     * @param product the product entity to save
     * @return ResponseEntity containing the saved product or error message
     */
    @Operation(summary = "Save a new product", description = "Adds a new product to the catalog (Admin only)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Product saved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid product data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Product>> saveProduct(@RequestBody Product product) {
        log.info("Save product request received: {}", product.getName());

        // Validate product data
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            log.error("Save product failed: Product name is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Product name is required", null));
        }

        if (product.getPrice() == null || product.getPrice() < 0) {
            log.error("Save product failed: Invalid price for product: {}", product.getName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Product price must be a positive value", null));
        }

        if (product.getStock() == null || product.getStock() < 0) {
            log.error("Save product failed: Invalid stock for product: {}", product.getName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Product stock must be a non-negative value", null));
        }

        try {
            Product savedProduct = productService.saveProduct(product);

            log.info("Product saved successfully with ID: {}", savedProduct.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Product saved successfully", savedProduct));

        } catch (ProductOperationException e) {
            log.error("Save product failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while saving product: {}", product.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while saving the product", null));
        }
    }

    /**
     * Saves a list of products to the catalog in bulk.
     * 
     * <p>This endpoint is restricted to ADMIN users only.
     * 
     * @param products the list of product entities to save
     * @return ResponseEntity containing the list of saved products or error message
     */
    @Operation(summary = "Bulk save products", description = "Adds multiple products to the catalog (Admin only)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Products saved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid product data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/save/list")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<Product>>> saveListProducts(@RequestBody List<Product> products) {
        log.info("Bulk save products request received with {} product(s)", products.size());

        // Validate input
        if (products == null || products.isEmpty()) {
            log.error("Bulk save failed: Product list is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Product list cannot be empty", null));
        }

        try {
            List<Product> savedProducts = productService.saveListProducts(products);

            log.info("Successfully saved {} product(s)", savedProducts.size());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("SUCCESS", "Products saved successfully", savedProducts));

        } catch (ProductOperationException e) {
            log.error("Bulk save products failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while saving products in bulk", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while saving products", null));
        }
    }

    /**
     * Retrieves a product by its unique identifier.
     * 
     * <p>This endpoint is publicly accessible (no authentication required).
     * 
     * @param request the HTTP request containing optional user headers
     * @param idProduct the unique identifier of the product to retrieve
     * @return ResponseEntity containing the product or error message
     */
    @Operation(summary = "Get product by ID", description = "Retrieves a single product by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{idProduct}")
    public ResponseEntity<ApiResponse<Product>> findProductById(
            HttpServletRequest request,
            @Parameter(description = "Product ID") @PathVariable("idProduct") Long idProduct) {

        log.info("Get product request received for ID: {}", idProduct);

        // Validate product ID
        if (idProduct == null || idProduct <= 0) {
            log.error("Get product failed: Invalid product ID: {}", idProduct);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("ERROR", "Invalid product ID", null));
        }

        try {
            Product product = productService.findProductById(idProduct);

            log.info("Successfully retrieved product with ID: {}", idProduct);

            return ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", "Product retrieved successfully", product));

        } catch (ProductNotFoundException e) {
            log.warn("Product not found with ID: {}", idProduct);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));

        } catch (Exception e) {
            log.error("Unexpected error while retrieving product with ID: {}", idProduct, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("ERROR", "An unexpected error occurred while retrieving the product", null));
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * Builds a CurrentUser object from authentication and request headers.
     * 
     * <p>Extracts user information from:
     * <ul>
     *   <li>Spring Security Authentication object (if available)</li>
     *   <li>HTTP request headers as fallback (X-USER-EMAIL, X-USER-ROLE, X-USER-ID)</li>
     * </ul>
     * 
     * @param auth the Spring Security authentication object
     * @param request the HTTP request containing user headers
     * @return CurrentUser object with user identity information
     */
    private CurrentUser buildCurrentUser(Authentication auth, HttpServletRequest request) {
        // Extract email from auth or header
        String email = (auth != null) ? auth.getName() : request.getHeader("X-USER-EMAIL");

        // Extract role from auth or header
        String role = (auth != null && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority()
                : request.getHeader("X-USER-ROLE");

        // Extract user ID from header
        String userIdHeader = request.getHeader("X-USER-ID");
        Long userId = (userIdHeader != null) ? Long.valueOf(userIdHeader) : null;

        log.debug("Built CurrentUser - email: {}, role: {}, userId: {}", email, role, userId);

        return new CurrentUser(email, role, userId);
    }

    // ==================== Response Record ====================

    /**
     * Generic API response structure for all endpoints.
     * 
     * @param <T> the type of data contained in the response
     * @param status the status of the operation (SUCCESS or ERROR)
     * @param message the response message
     * @param data the response data (null if error)
     */
    public record ApiResponse<T>(String status, String message, T data) {}

}
