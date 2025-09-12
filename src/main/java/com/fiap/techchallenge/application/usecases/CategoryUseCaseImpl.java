package com.fiap.techchallenge.application.usecases;

import com.fiap.techchallenge.domain.entities.Category;
import com.fiap.techchallenge.domain.entities.Product;
import com.fiap.techchallenge.domain.exception.DomainException;
import com.fiap.techchallenge.domain.exception.NotFoundException;
import com.fiap.techchallenge.domain.repositories.CategoryRepository;
import com.fiap.techchallenge.domain.repositories.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryUseCaseImpl implements CategoryUseCase {

    private static final String RECORD_NOT_FOUND_MESSAGE = "Record not found";

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryUseCaseImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Category createCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new DomainException("Category with name " + name + " already exists");
        }

        Category category = Category.builder()
                .id(UUID.randomUUID())
                .name(name)
                .build();

        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(UUID id, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new DomainException("Category name cannot be blank.");
        }

        return categoryRepository.findById(id)
                .map(category -> {
                    Category updatedCategory = Category.builder()
                            .id(category.getId())
                            .name(name)
                            .build();
                    return categoryRepository.save(updatedCategory);
                })
                .orElseThrow(() -> new NotFoundException(RECORD_NOT_FOUND_MESSAGE));
    }

    @Override
    public Optional<Category> findById(UUID id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            throw new NotFoundException(RECORD_NOT_FOUND_MESSAGE);
        }
        return category;
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        if (categoryRepository.findById(id).isEmpty()) {
            throw new NotFoundException(RECORD_NOT_FOUND_MESSAGE);
        }

        // Verifica se a categoria está vinculada a produtos
        List<Product> productsInCategory = productRepository.findByCategoryId(id);
        if (!productsInCategory.isEmpty()) {
            throw new DomainException("Não é possível deletar a categoria pois ela está vinculada a um ou mais produtos");
        }

        categoryRepository.deleteById(id);
    }
}
