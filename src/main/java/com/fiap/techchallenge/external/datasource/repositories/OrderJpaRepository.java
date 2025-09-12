package com.fiap.techchallenge.external.datasource.repositories;

import com.fiap.techchallenge.external.datasource.entities.OrderJpaEntity;
import com.fiap.techchallenge.external.datasource.entities.OrderJpaEntity.OrderStatusJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {

    @Query("SELECT o FROM OrderJpaEntity o " +
       "WHERE o.status <> 'FINISHED' " +
       "ORDER BY " +
       "CASE o.status " +
       "WHEN 'READY' THEN 1 " +
       "WHEN 'IN_PREPARATION' THEN 2 " +
       "WHEN 'RECEIVED' THEN 3 " +
       "ELSE 4 END, " +
       "o.createdAt ASC")
    List<OrderJpaEntity> findByOptionalStatus(@Param("status") OrderStatusJpa status);

    Optional<OrderJpaEntity> findByIdPayment(@Param("idPayment") Long idPayment);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM OrderJpaEntity o JOIN o.items i WHERE i.productId = :productId")
    boolean existsByItemsProductId(@Param("productId") UUID productId);
}
