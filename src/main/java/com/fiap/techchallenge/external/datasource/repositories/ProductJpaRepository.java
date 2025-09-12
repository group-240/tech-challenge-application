
package com.fiap.techchallenge.external.datasource.repositories;

import com.fiap.techchallenge.external.datasource.entities.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {
    List<ProductJpaEntity> findByNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM ProductJpaEntity p WHERE p.category.id = :categoryId")
    List<ProductJpaEntity> findByCategoryId(@Param("categoryId") UUID categoryId);
}
