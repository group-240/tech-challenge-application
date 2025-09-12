package com.fiap.techchallenge.domain.repositories;

import com.fiap.techchallenge.domain.entities.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    List<Product> findByName(String name);
    List<Product> findByCategoryId(UUID categoryId);
    List<Product> findAll();
    void deleteById(UUID id);
}
