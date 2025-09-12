package com.fiap.techchallenge.application.usecases;

import com.fiap.techchallenge.domain.entities.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductUseCase {
    Product createProduct(String name, String description, BigDecimal price, UUID categoryId);
    Optional<Product> findProductById(UUID id);
    List<Product> findProductsByName(String name);
    List<Product> findAllProducts();
    List<Product> findProductsByCategory(UUID categoryId);
    Product updateProduct(UUID id, String name, String description, BigDecimal price, UUID categoryId);
    void deleteProduct(UUID id);
}
