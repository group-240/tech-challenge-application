package com.fiap.techchallenge.application.usecases;

import com.fiap.techchallenge.domain.entities.Category;
import com.fiap.techchallenge.domain.entities.Product;
import com.fiap.techchallenge.domain.exception.NotFoundException;
import com.fiap.techchallenge.domain.exception.ProductLinkedToOrderException;
import com.fiap.techchallenge.domain.repositories.CategoryRepository;
import com.fiap.techchallenge.domain.repositories.OrderRepository;
import com.fiap.techchallenge.domain.repositories.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductUseCaseImpl implements ProductUseCase {

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

        return productRepository.save(product);
    }

    @Override
    public Optional<Product> findProductById(UUID id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            throw new NotFoundException(RECORD_NOT_FOUND_MESSAGE);
        }
        return product;
    }

    @Override
    public List<Product> findProductsByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> findProductsByCategory(UUID categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public Product updateProduct(UUID id, String name, String description, BigDecimal price, UUID categoryId) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(RECORD_NOT_FOUND_MESSAGE));

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException(RECORD_NOT_FOUND_MESSAGE));
        }

        Product updatedProduct = existingProduct.update(name, description, price, category);
        return productRepository.save(updatedProduct);
    }

    @Override
    public void deleteProduct(UUID id) {
        if (productRepository.findById(id).isEmpty()) {
            throw new NotFoundException(RECORD_NOT_FOUND_MESSAGE);
        }

        if (orderRepository.existsByProductId(id)) {
            throw new ProductLinkedToOrderException(PRODUCT_LINKED_TO_ORDER_MESSAGE);
        }

        productRepository.deleteById(id);
    }
}
