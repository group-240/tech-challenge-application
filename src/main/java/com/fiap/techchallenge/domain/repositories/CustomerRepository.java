package com.fiap.techchallenge.domain.repositories;

import com.fiap.techchallenge.domain.entities.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(UUID id);
    Optional<Customer> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    List<Customer> findAll();
}
