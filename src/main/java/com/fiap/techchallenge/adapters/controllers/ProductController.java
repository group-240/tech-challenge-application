package com.fiap.techchallenge.adapters.controllers;

import com.fiap.techchallenge.application.usecases.ProductUseCase;
import com.fiap.techchallenge.domain.entities.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductController {

    private final ProductUseCase productUseCase;

    public ProductController(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    public Product createProduct(String name, String description, BigDecimal price, UUID categoryId) {
        return productUseCase.createProduct(name, description, price, categoryId);
    }

    public Optional<Product> findProductById(UUID id) {
        return productUseCase.findProductById(id);
    }

    public List<Product> findProductsByName(String name) {
        return productUseCase.findProductsByName(name);
    }

    public List<Product> findAllProducts() {
        return productUseCase.findAllProducts();
    }

    public List<Product> findProductsByCategory(UUID categoryId) {
        return productUseCase.findProductsByCategory(categoryId);
    }

    public Product updateProduct(UUID id, String name, String description, BigDecimal price, UUID categoryId) {
        return productUseCase.updateProduct(id, name, description, price, categoryId);
    }

    public void deleteProduct(UUID id) {
        productUseCase.deleteProduct(id);
    }
}
