package com.fiap.techchallenge.application.usecases.mappers;

import com.fiap.techchallenge.domain.entities.Category;
import com.fiap.techchallenge.external.datasource.entities.CategoryJpaEntity;

public class CategoryMapper {

    public static CategoryJpaEntity toJpaEntity(Category category) {
        if (category == null) return null;

        return new CategoryJpaEntity(
            category.getId(),
            category.getName()
        );
    }

    public static Category toDomainEntity(CategoryJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        return Category.builder()
            .id(jpaEntity.getId())
            .name(jpaEntity.getName())
            .build();
    }
}
