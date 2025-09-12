package com.fiap.techchallenge.application.usecases.mappers;

import com.fiap.techchallenge.domain.entities.Customer;
import com.fiap.techchallenge.external.datasource.entities.CustomerJpaEntity;

public class CustomerMapper {

    public static CustomerJpaEntity toJpaEntity(Customer customer) {
        if (customer == null) return null;

        return new CustomerJpaEntity(
            customer.getId(),
            customer.getName(),
            customer.getEmail(),
            customer.getCpf()
        );
    }

    public static Customer toDomainEntity(CustomerJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        return Customer.builder()
            .id(jpaEntity.getId())
            .name(jpaEntity.getName())
            .email(jpaEntity.getEmail())
            .cpf(jpaEntity.getCpf())
            .build();
    }
}
