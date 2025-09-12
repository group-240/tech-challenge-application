package com.fiap.techchallenge.adapters.gateway;

import com.fiap.techchallenge.application.usecases.mappers.ProductMapper;
import com.fiap.techchallenge.domain.entities.Product;
import com.fiap.techchallenge.domain.repositories.ProductRepository;
import com.fiap.techchallenge.external.datasource.repositories.ProductJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProductRepositoryGateway implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    public ProductRepositoryGateway(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public Product save(Product product) {
        var jpaEntity = ProductMapper.toJpaEntity(product);
        var savedEntity = productJpaRepository.save(jpaEntity);
        return ProductMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return productJpaRepository.findById(id)
                .map(ProductMapper::toDomainEntity);
    }

    @Override
    public List<Product> findByName(String name) {
        return productJpaRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(ProductMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByCategoryId(UUID categoryId) {
        return productJpaRepository.findByCategoryId(categoryId)
                .stream()
                .map(ProductMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll()
                .stream()
                .map(ProductMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        productJpaRepository.deleteById(id);
    }
}
