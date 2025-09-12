package com.fiap.techchallenge.application.usecases;

import com.fiap.techchallenge.domain.entities.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryUseCase {
    Category createCategory(String name);
    Category updateCategory(UUID id, String name);
    Optional<Category> findById(UUID id);
    List<Category> findAll();
    void deleteById(UUID id);
}
