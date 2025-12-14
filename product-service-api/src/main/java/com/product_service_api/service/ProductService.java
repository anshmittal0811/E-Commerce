package com.product_service_api.service;

import java.util.List;

import com.product_service_api.entity.Product;

/**
 * Service interface for product management operations.
 * 
 * <p>This interface defines the contract for:
 * <ul>
 *   <li>Product retrieval operations</li>
 *   <li>Product creation and updates</li>
 *   <li>Stock management</li>
 * </ul>
 * 
 * <p>Implementations should handle proper exception throwing for error cases.
 */
public interface ProductService {

    /**
     * Retrieves all products from the catalog.
     * 
     * @return list of all products in the catalog
     */
    List<Product> findAllProducts();

    /**
     * Updates the stock quantity for a specific product.
     * 
     * @param idProduct the unique identifier of the product
     * @param quantity the quantity to deduct from current stock
     * @return the updated product entity
     */
    Product updateStockProduct(Long idProduct, Integer quantity);

    /**
     * Retrieves a product by its unique identifier.
     * 
     * @param idProduct the product ID to search for
     * @return the product if found
     */
    Product findProductById(Long idProduct);

    /**
     * Saves a new product to the catalog.
     * 
     * @param product the product entity to save
     * @return the saved product with generated ID
     */
    Product saveProduct(Product product);

    /**
     * Saves a list of products to the catalog in bulk.
     * 
     * @param products the list of products to save
     * @return the list of saved products with generated IDs
     */
    List<Product> saveListProducts(List<Product> products);

}
