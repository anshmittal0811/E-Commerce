package com.product_service_api.service;

import java.util.List;

import com.product_service_api.exception.ProductNotFoundException;
import com.product_service_api.exception.ProductOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.product_service_api.entity.Product;
import com.product_service_api.repository.ProductRepository;

/**
 * Implementation of the ProductService interface.
 * 
 * <p>This service handles all product-related business logic including:
 * <ul>
 *   <li>Product retrieval operations</li>
 *   <li>Product creation and updates</li>
 *   <li>Stock management</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    /**
     * Retrieves all products from the catalog.
     * 
     * @return list of all products
     * @throws ProductOperationException if an error occurs during retrieval
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> findAllProducts() {
        log.debug("Fetching all products from catalog");

        try {
            List<Product> products = productRepository.findAll();
            log.debug("Found {} product(s) in catalog", products.size());
            return products;

        } catch (Exception e) {
            log.error("Error occurred while fetching all products", e);
            throw new ProductOperationException("Failed to retrieve products from catalog", e);
        }
    }

    /**
     * Retrieves a product by its unique identifier.
     * 
     * @param idProduct the product ID to search for
     * @return the product if found
     * @throws ProductNotFoundException if the product does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public Product findProductById(Long idProduct) {
        log.debug("Fetching product with ID: {}", idProduct);

        return productRepository.findById(idProduct)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", idProduct);
                    return new ProductNotFoundException(idProduct);
                });
    }

    /**
     * Saves a new product to the catalog.
     * 
     * @param product the product entity to save
     * @return the saved product with generated ID
     * @throws ProductOperationException if an error occurs during save
     */
    @Override
    @Transactional
    public Product saveProduct(Product product) {
        log.debug("Saving product: {}", product.getName());

        try {
            Product savedProduct = productRepository.save(product);
            log.info("Product saved successfully with ID: {}", savedProduct.getId());
            return savedProduct;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while saving product: {}", product.getName(), e);
            throw new ProductOperationException("Product with name '" + product.getName() + "' already exists", e);

        } catch (Exception e) {
            log.error("Error occurred while saving product: {}", product.getName(), e);
            throw new ProductOperationException("Failed to save product: " + product.getName(), e);
        }
    }

    /**
     * Saves a list of products to the catalog in bulk.
     * 
     * @param products the list of products to save
     * @return the list of saved products with generated IDs
     * @throws ProductOperationException if an error occurs during save
     */
    @Override
    @Transactional
    public List<Product> saveListProducts(List<Product> products) {
        log.debug("Saving {} product(s) in bulk", products.size());

        try {
            List<Product> savedProducts = productRepository.saveAll(products);
            log.info("Successfully saved {} product(s) in bulk", savedProducts.size());
            return savedProducts;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while saving products in bulk", e);
            throw new ProductOperationException("One or more products have duplicate names", e);

        } catch (Exception e) {
            log.error("Error occurred while saving products in bulk", e);
            throw new ProductOperationException("Failed to save products in bulk", e);
        }
    }

    /**
     * Updates the stock for a specific product.
     * 
     * <p>This method decreases the stock by the specified quantity.
     * 
     * @param idProduct the product ID to update
     * @param quantity the quantity to deduct from stock
     * @return the updated product
     * @throws ProductNotFoundException if the product does not exist
     * @throws ProductOperationException if stock is insufficient or an error occurs
     */
    @Override
    @Transactional
    public Product updateStockProduct(Long idProduct, Integer quantity) {
        log.debug("Updating stock for product ID: {} with quantity: {}", idProduct, quantity);

        // Find the product
        Product product = productRepository.findById(idProduct)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", idProduct);
                    return new ProductNotFoundException(idProduct);
                });

        Integer currentStock = product.getStock();

        // Validate sufficient stock
        if (currentStock < quantity) {
            log.warn("Insufficient stock for product ID: {}. Current: {}, Requested: {}",
                    idProduct, currentStock, quantity);
            throw new ProductOperationException(
                    String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                            product.getName(), currentStock, quantity));
        }

        try {
            // Update stock
            product.setStock(currentStock - quantity);
            Product updatedProduct = productRepository.save(product);

            log.info("Stock updated for product ID: {}. Previous: {}, New: {}",
                    idProduct, currentStock, updatedProduct.getStock());

            return updatedProduct;

        } catch (Exception e) {
            log.error("Error occurred while updating stock for product ID: {}", idProduct, e);
            throw new ProductOperationException("Failed to update stock for product ID: " + idProduct, e);
        }
    }

}
