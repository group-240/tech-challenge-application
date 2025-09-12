package com.fiap.techchallenge.adapters.gateway;

import com.fiap.techchallenge.application.usecases.mappers.CustomerMapper;
import com.fiap.techchallenge.domain.entities.Customer;
import com.fiap.techchallenge.domain.repositories.CustomerRepository;
import com.fiap.techchallenge.external.datasource.repositories.CustomerJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CustomerRepositoryGateway implements CustomerRepository {

    private final CustomerJpaRepository customerJpaRepository;

    public CustomerRepositoryGateway(CustomerJpaRepository customerJpaRepository) {
        this.customerJpaRepository = customerJpaRepository;
    }

    @Override
    public Customer save(Customer customer) {
        var jpaEntity = CustomerMapper.toJpaEntity(customer);
        var savedEntity = customerJpaRepository.save(jpaEntity);
        return CustomerMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return customerJpaRepository.findById(id)
                .map(CustomerMapper::toDomainEntity);
    }

    @Override
    public Optional<Customer> findByCpf(String cpf) {
        return customerJpaRepository.findByCpf(cpf)
                .map(CustomerMapper::toDomainEntity);
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return customerJpaRepository.existsByCpf(cpf);
    }

    @Override
    public List<Customer> findAll() {
        return customerJpaRepository.findAll()
                .stream()
                .map(CustomerMapper::toDomainEntity)
                .collect(Collectors.toList());
    }
}
