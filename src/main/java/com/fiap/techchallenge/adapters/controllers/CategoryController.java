package com.fiap.techchallenge.adapters.controllers;

import com.fiap.techchallenge.application.usecases.CategoryUseCase;
import com.fiap.techchallenge.domain.entities.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    public CategoryController(CategoryUseCase categoryUseCase) {
        this.categoryUseCase = categoryUseCase;
    }

    public Category createCategory(String name) {
        return categoryUseCase.createCategory(name);
    }

    public Category updateCategory(UUID id, String name) {
        return categoryUseCase.updateCategory(id, name);
    }

    public Optional<Category> findById(UUID id) {
        return categoryUseCase.findById(id);
    }

    public List<Category> findAll() {
        return categoryUseCase.findAll();
    }

    public void deleteById(UUID id) {
        categoryUseCase.deleteById(id);
    }
}
