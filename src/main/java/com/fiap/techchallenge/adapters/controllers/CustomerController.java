package com.fiap.techchallenge.adapters.controllers;

import com.fiap.techchallenge.application.usecases.CustomerUseCase;
import com.fiap.techchallenge.domain.entities.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CustomerController {

    private final CustomerUseCase customerUseCase;

    public CustomerController(CustomerUseCase customerUseCase) {
        this.customerUseCase = customerUseCase;
    }

    public Customer registerCustomer(String name, String email, String cpf) {
        return customerUseCase.registerCustomer(name, email, cpf);
    }

    public Optional<Customer> findCustomerByCpf(String cpf) {
        return customerUseCase.findCustomerByCpf(cpf);
    }

    public Optional<Customer> findCustomerById(UUID id) {
        return customerUseCase.findCustomerById(id);
    }

    public List<Customer> findAllCustomers() {
        return customerUseCase.findCustomerAll();
    }
}
