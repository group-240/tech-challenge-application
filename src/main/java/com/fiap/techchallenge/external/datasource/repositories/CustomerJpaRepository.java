package com.fiap.techchallenge.external.datasource.repositories;

import com.fiap.techchallenge.external.datasource.entities.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, UUID> {
    Optional<CustomerJpaEntity> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
}
