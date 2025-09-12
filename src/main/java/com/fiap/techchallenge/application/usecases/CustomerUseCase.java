package com.fiap.techchallenge.application.usecases;

import com.fiap.techchallenge.domain.entities.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerUseCase {
    Customer registerCustomer(String name, String email, String cpf);
    Optional<Customer> findCustomerByCpf(String cpf);
    Optional<Customer> findCustomerById(UUID id);
    List<Customer> findCustomerAll();
}
