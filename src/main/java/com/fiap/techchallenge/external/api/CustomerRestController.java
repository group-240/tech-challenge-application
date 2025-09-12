package com.fiap.techchallenge.external.api;

import com.fiap.techchallenge.adapters.controllers.CustomerController;
import com.fiap.techchallenge.domain.entities.Customer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
@Tag(name = "Customers", description = "API para gerenciamento de clientes")
public class CustomerRestController {

    private final CustomerController customerController;

    public CustomerRestController(CustomerController customerController) {
        this.customerController = customerController;
    }

    @PostMapping(consumes = "application/json")
    @Operation(summary = "Criar novo cliente", description = "Registra um novo cliente no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Customer.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "409", description = "Cliente com CPF já existe")
    })
    public ResponseEntity<Customer> registerCustomer(@RequestBody @Valid CustomerRequestDTO customerRequest) {
        Customer customer = customerController.registerCustomer(
                customerRequest.getName(),
                customerRequest.getEmail(),
                customerRequest.getCpf()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Busca um cliente específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Customer.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<Customer> findCustomerById(@PathVariable UUID id) {
        return customerController.findCustomerById(id)
                .map(customer -> ResponseEntity.ok(customer))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar cliente por CPF", description = "Busca um cliente específico pelo seu CPF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Customer.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<Customer> findCustomerByCpf(@PathVariable String cpf) {
        return customerController.findCustomerByCpf(cpf)
                .map(customer -> ResponseEntity.ok(customer))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar todos os clientes", description = "Retorna todos os clientes cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes retornada com sucesso")
    })
    public ResponseEntity<List<Customer>> findAllCustomers() {
        List<Customer> customers = customerController.findAllCustomers();
        return ResponseEntity.ok(customers);
    }

    // DTOs internos para este controller
    public static class CustomerRequestDTO {
        private String name;
        private String email;
        private String cpf;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCpf() { return cpf; }
        public void setCpf(String cpf) { this.cpf = cpf; }
    }
}
