package com.fiap.techchallenge.adapters.presenters;

import com.fiap.techchallenge.domain.entities.Customer;

import java.util.List;
import java.util.stream.Collectors;

public class CustomerPresenter {

    public static CustomerResponseDTO toResponseDTO(Customer customer) {
        return new CustomerResponseDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getCpf()
        );
    }

    public static List<CustomerResponseDTO> toResponseDTOList(List<Customer> customers) {
        return customers.stream()
                .map(CustomerPresenter::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static class CustomerResponseDTO {
        private java.util.UUID id;
        private String name;
        private String email;
        private String cpf;

        public CustomerResponseDTO() {}

        public CustomerResponseDTO(java.util.UUID id, String name, String email, String cpf) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.cpf = cpf;
        }

        // Getters and Setters
        public java.util.UUID getId() { return id; }
        public void setId(java.util.UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCpf() { return cpf; }
        public void setCpf(String cpf) { this.cpf = cpf; }
    }
}
