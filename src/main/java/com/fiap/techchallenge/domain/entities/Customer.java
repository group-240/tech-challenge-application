package com.fiap.techchallenge.domain.entities;

import com.fiap.techchallenge.domain.exception.InvalidCpfException;
import com.fiap.techchallenge.domain.exception.InvalidEmailException;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;


public final class Customer {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final int CPF_LENGTH = 11;
    
    private final UUID id;
    private final String name;
    private final String email;
    private final String cpf;

    private Customer(UUID id, String name, String email, String cpf) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.name = validateName(name);
        this.email = validateEmail(email);
        this.cpf = validateCpf(cpf);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        return name.trim();
    }

    private String validateEmail(String email) {
        if (email == null || email.trim().isEmpty())  {
            return "";
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new InvalidEmailException("Invalid email format: " + email);
        }
        return email.trim().toLowerCase();
    }

    private String validateCpf(String cpf) {
        if (cpf == null) {
            throw new InvalidCpfException("CPF cannot be null");
        }

        String cleanCpf = cpf.replaceAll("\\D", "");
        if (cleanCpf.length() != CPF_LENGTH) {
            throw new InvalidCpfException("CPF must contain exactly 11 digits");
        }

        if (!isValidCpfChecksum(cleanCpf)) {
            throw new InvalidCpfException("Invalid CPF checksum");
        }

        return cleanCpf;
    }

    private boolean isValidCpfChecksum(String cpf) {
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int firstDigit = 11 - (sum % 11);
        firstDigit = (firstDigit >= 10) ? 0 : firstDigit;

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int secondDigit = 11 - (sum % 11);
        secondDigit = (secondDigit >= 10) ? 0 : secondDigit;

        return firstDigit == Character.getNumericValue(cpf.charAt(9)) &&
               secondDigit == Character.getNumericValue(cpf.charAt(10));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id) &&
               Objects.equals(cpf, customer.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cpf);
    }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", name='" + name + "', email='" + email + "', cpf='" + cpf + "'}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String name;
        private String email;
        private String cpf;

        private Builder() {}

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public Customer build() {
            return new Customer(id, name, email, cpf);
        }
    }
}