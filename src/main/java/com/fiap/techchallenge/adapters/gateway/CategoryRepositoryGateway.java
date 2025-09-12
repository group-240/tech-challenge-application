package com.fiap.techchallenge.adapters.gateway;

import com.fiap.techchallenge.application.usecases.mappers.CategoryMapper;
import com.fiap.techchallenge.domain.entities.Category;
import com.fiap.techchallenge.domain.repositories.CategoryRepository;
import com.fiap.techchallenge.external.datasource.repositories.CategoryJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CategoryRepositoryGateway implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    public CategoryRepositoryGateway(CategoryJpaRepository categoryJpaRepository) {
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public Category save(Category category) {
        var jpaEntity = CategoryMapper.toJpaEntity(category);
        var savedEntity = categoryJpaRepository.save(jpaEntity);
        return CategoryMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return categoryJpaRepository.findById(id)
                .map(CategoryMapper::toDomainEntity);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryJpaRepository.existsByName(name);
    }

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAll()
                .stream()
                .map(CategoryMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        categoryJpaRepository.deleteById(id);
    }
}
