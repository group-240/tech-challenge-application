package com.fiap.techchallenge.application.usecases;

import com.fiap.techchallenge.domain.entities.Category;
import com.fiap.techchallenge.domain.entities.Product;
import com.fiap.techchallenge.domain.exception.NotFoundException;
import com.fiap.techchallenge.domain.exception.ProductLinkedToOrderException;
import com.fiap.techchallenge.domain.repositories.CategoryRepository;
import com.fiap.techchallenge.domain.repositories.OrderRepository;
import com.fiap.techchallenge.domain.repositories.ProductRepository;
import com.fiap.techchallenge.infrastructure.logging.LogCategory;
import com.fiap.techchallenge.infrastructure.logging.StructuredLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductUseCaseImpl implements ProductUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ProductUseCaseImpl.class);
    private static final String RECORD_NOT_FOUND_MESSAGE = "Record not found";
    private static final String CATEGORY_NOT_FOUND_MESSAGE = "Category Record not found";
    private static final String PRODUCT_LINKED_TO_ORDER_MESSAGE = "Product is already linked to an order and cannot be deleted";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;

    public ProductUseCaseImpl(ProductRepository productRepository, CategoryRepository categoryRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public Product createProduct(String name, String description, BigDecimal price, UUID categoryId) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("CreateProduct");
            StructuredLogger.put("categoryId", categoryId.toString());
            StructuredLogger.put("productName", name);
            
            logger.info("Product creation started: name={}, categoryId={}, price={}", 
                       name, categoryId, price);
            
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_MESSAGE));

            Product product = Product.builder()
                    .id(UUID.randomUUID())
                    .name(name)
                    .description(description)
                    .price(price)
                    .category(category)
                    .active(true)
                    .build();

            Product savedProduct = productRepository.save(product);
            StructuredLogger.setProductId(savedProduct.getId().toString());
            
            logger.info("Product created successfully: productId={}, name={}", 
                       savedProduct.getId(), name);
            
            return savedProduct;
            
        } catch (NotFoundException e) {
            logger.warn("Product creation failed - category not found: categoryId={}", categoryId);
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("PRODUCT_CREATION_FAILED", e.getMessage());
            logger.error("Failed to create product: name={}", name, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public Optional<Product> findProductById(UUID id) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("FindProductById");
            StructuredLogger.setProductId(id.toString());
            
            Optional<Product> product = productRepository.findById(id);
            if (product.isEmpty()) {
                logger.warn("Product not found: productId={}", id);
                throw new NotFoundException(RECORD_NOT_FOUND_MESSAGE);
            }
            
            logger.info("Product found: productId={}, name={}", id, product.get().getName());
            return product;
            
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("PRODUCT_FIND_FAILED", e.getMessage());
            logger.error("Failed to find product: productId={}", id, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public List<Product> findProductsByName(String name) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("FindProductsByName");
            
            List<Product> products = productRepository.findByName(name);
            logger.info("Products found by name: name={}, count={}", name, products.size());
            
            return products;
            
        } catch (Exception e) {
            StructuredLogger.setError("PRODUCT_SEARCH_FAILED", e.getMessage());
            logger.error("Failed to find products by name: name={}", name, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public List<Product> findAllProducts() {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("FindAllProducts");
            
            List<Product> products = productRepository.findAll();
            logger.info("All products listed: count={}", products.size());
            
            return products;
            
        } catch (Exception e) {
            StructuredLogger.setError("PRODUCT_LIST_FAILED", e.getMessage());
            logger.error("Failed to list all products", e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public List<Product> findProductsByCategory(UUID categoryId) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("FindProductsByCategory");
            StructuredLogger.put("categoryId", categoryId.toString());
            
            List<Product> products = productRepository.findByCategoryId(categoryId);
            logger.info("Products found by category: categoryId={}, count={}", 
                       categoryId, products.size());
            
            return products;
            
        } catch (Exception e) {
            StructuredLogger.setError("PRODUCT_SEARCH_FAILED", e.getMessage());
            logger.error("Failed to find products by category: categoryId={}", categoryId, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public Product updateProduct(UUID id, String name, String description, BigDecimal price, UUID categoryId) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("UpdateProduct");
            StructuredLogger.setProductId(id.toString());
            
            logger.info("Product update started: productId={}", id);
            
            Product existingProduct = productRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(RECORD_NOT_FOUND_MESSAGE));

            Category category = null;
            if (categoryId != null) {
                category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new NotFoundException(RECORD_NOT_FOUND_MESSAGE));
            }

            Product updatedProduct = existingProduct.update(name, description, price, category);
            Product savedProduct = productRepository.save(updatedProduct);
            
            logger.info("Product updated successfully: productId={}, name={}", id, name);
            
            return savedProduct;
            
        } catch (NotFoundException e) {
            logger.warn("Product update failed - not found: productId={}", id);
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("PRODUCT_UPDATE_FAILED", e.getMessage());
            logger.error("Failed to update product: productId={}", id, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public void deleteProduct(UUID id) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("DeleteProduct");
            StructuredLogger.setProductId(id.toString());
            
            logger.info("Product deletion started: productId={}", id);
            
            if (productRepository.findById(id).isEmpty()) {
                logger.warn("Product deletion failed - not found: productId={}", id);
                throw new NotFoundException(RECORD_NOT_FOUND_MESSAGE);
            }

            if (orderRepository.existsByProductId(id)) {
                logger.warn("Product deletion failed - linked to orders: productId={}", id);
                throw new ProductLinkedToOrderException(PRODUCT_LINKED_TO_ORDER_MESSAGE);
            }

            productRepository.deleteById(id);
            logger.info("Product deleted successfully: productId={}", id);
            
        } catch (NotFoundException | ProductLinkedToOrderException e) {
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("PRODUCT_DELETION_FAILED", e.getMessage());
            logger.error("Failed to delete product: productId={}", id, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }
}
