package com.fiap.techchallenge.external.datasource.repositories;

import com.fiap.techchallenge.external.datasource.entities.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {
    boolean existsByName(String name);
}
