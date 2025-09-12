package com.fiap.techchallenge.application.usecases.mappers;

import com.fiap.techchallenge.domain.entities.Category;
import com.fiap.techchallenge.domain.entities.Product;
import com.fiap.techchallenge.external.datasource.entities.CategoryJpaEntity;
import com.fiap.techchallenge.external.datasource.entities.ProductJpaEntity;

public class ProductMapper {

    public static ProductJpaEntity toJpaEntity(Product product) {
        if (product == null) return null;

        CategoryJpaEntity categoryJpa = null;
        if (product.getCategory() != null) {
            categoryJpa = CategoryMapper.toJpaEntity(product.getCategory());
        }

        return new ProductJpaEntity(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            categoryJpa,
            product.isActive()
        );
    }

    public static Product toDomainEntity(ProductJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        Category category = null;
        if (jpaEntity.getCategory() != null) {
            category = CategoryMapper.toDomainEntity(jpaEntity.getCategory());
        }

        return Product.builder()
            .id(jpaEntity.getId())
            .name(jpaEntity.getName())
            .description(jpaEntity.getDescription())
            .price(jpaEntity.getPrice())
            .category(category)
            .active(jpaEntity.isActive())
            .build();
    }
}
