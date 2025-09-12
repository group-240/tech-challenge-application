package com.fiap.techchallenge.domain.repositories;

import com.fiap.techchallenge.domain.entities.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    boolean existsByName(String name);
    List<Category> findAll();
    void deleteById(UUID id);
}
