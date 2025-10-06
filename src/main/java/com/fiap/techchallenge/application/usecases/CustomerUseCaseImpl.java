package com.fiap.techchallenge.application.usecases;

import com.fiap.techchallenge.domain.entities.Customer;
import com.fiap.techchallenge.domain.exception.DomainException;
import com.fiap.techchallenge.domain.exception.NotFoundException;
import com.fiap.techchallenge.domain.repositories.CustomerRepository;
import com.fiap.techchallenge.external.cognito.CognitoService;
import com.fiap.techchallenge.infrastructure.logging.LogCategory;
import com.fiap.techchallenge.infrastructure.logging.StructuredLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CustomerUseCaseImpl implements CustomerUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CustomerUseCaseImpl.class);
    private static final String RECORD_NOT_FOUND_MESSAGE = "Record not found";

    private final CustomerRepository customerRepository;
    private final CognitoService cognitoService;

    public CustomerUseCaseImpl(CustomerRepository customerRepository, CognitoService cognitoService) {
        this.customerRepository = customerRepository;
        this.cognitoService = cognitoService;
    }

    @Override
    public Customer registerCustomer(String name, String email, String cpf) {
        long startTime = System.currentTimeMillis();
        
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("RegisterCustomer");
            StructuredLogger.put("cpf", cpf);
            StructuredLogger.put("email", email);
            
            logger.info("Customer registration started: cpf={}, email={}", cpf, email);
            
            if (customerRepository.existsByCpf(cpf)) {
                logger.warn("Customer registration failed - CPF already exists: cpf={}", cpf);
                throw new DomainException("Customer with CPF " + cpf + " already exists");
            }

            Customer customer = Customer.builder()
                    .id(UUID.randomUUID())
                    .name(name)
                    .email(email)
                    .cpf(cpf)
                    .build();

            // Salva no banco de dados
            Customer savedCustomer = customerRepository.save(customer);
            StructuredLogger.setCustomerId(savedCustomer.getId().toString());
            
            // Cria usuário no Cognito simultaneamente
            try {
                cognitoService.createUser(cpf, email, name);
                logger.info("Customer created in Cognito successfully: customerId={}", savedCustomer.getId());
            } catch (Exception e) {
                StructuredLogger.setCategory(LogCategory.INTEGRATION);
                logger.error("Failed to create customer in Cognito (database record created): customerId={}, cpf={}", 
                           savedCustomer.getId(), cpf, e);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            StructuredLogger.setDuration(duration);
            logger.info("Customer registered successfully: customerId={}, cpf={}", 
                       savedCustomer.getId(), cpf);
            
            return savedCustomer;
            
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("CUSTOMER_REGISTRATION_FAILED", e.getMessage());
            logger.error("Failed to register customer: cpf={}, email={}", cpf, email, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public Optional<Customer> findCustomerByCpf(String cpf) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("FindCustomerByCpf");
            StructuredLogger.put("cpf", cpf);
            
            Optional<Customer> customer = customerRepository.findByCpf(cpf);
            if (customer.isEmpty()) {
                logger.warn("Customer not found by CPF: cpf={}", cpf);
                throw new NotFoundException(RECORD_NOT_FOUND_MESSAGE);
            }
            
            logger.info("Customer found by CPF: customerId={}, cpf={}", 
                       customer.get().getId(), cpf);
            return customer;
            
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("CUSTOMER_FIND_FAILED", e.getMessage());
            logger.error("Failed to find customer by CPF: cpf={}", cpf, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public Optional<Customer> findCustomerById(UUID id) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("FindCustomerById");
            StructuredLogger.setCustomerId(id.toString());
            
            Optional<Customer> customer = customerRepository.findById(id);
            if (customer.isEmpty()) {
                logger.warn("Customer not found by ID: customerId={}", id);
                throw new NotFoundException(RECORD_NOT_FOUND_MESSAGE);
            }
            
            logger.info("Customer found by ID: customerId={}", id);
            return customer;
            
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("CUSTOMER_FIND_FAILED", e.getMessage());
            logger.error("Failed to find customer by ID: customerId={}", id, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public List<Customer> findCustomerAll() {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("FindAllCustomers");
            
            List<Customer> customers = customerRepository.findAll();
            logger.info("Customers listed: count={}", customers.size());
            
            return customers;
            
        } catch (Exception e) {
            StructuredLogger.setError("CUSTOMER_LIST_FAILED", e.getMessage());
            logger.error("Failed to list customers", e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }
}
