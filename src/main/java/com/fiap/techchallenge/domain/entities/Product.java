package com.fiap.techchallenge.domain.entities;

import java.math.BigDecimal;
import java.util.UUID;

public class Product {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Category category;
    private boolean active;

    public Product() {}

    public Product(UUID id, String name, String description, BigDecimal price, Category category, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.active = active;
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Category getCategory() { return category; }
    public boolean isActive() { return active; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setCategory(Category category) { this.category = category; }
    public void setActive(boolean active) { this.active = active; }

    // Business methods
    public Product update(String name, String description, BigDecimal price, Category category) {
        return new Product(
            this.id,
            name != null ? name : this.name,
            description != null ? description : this.description,
            price != null ? price : this.price,
            category != null ? category : this.category,
            this.active
        );
    }

    public Product activate() {
        return new Product(this.id, this.name, this.description, this.price, this.category, true);
    }

    public Product deactivate() {
        return new Product(this.id, this.name, this.description, this.price, this.category, false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String name;
        private String description;
        private BigDecimal price;
        private Category category;
        private boolean active;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder category(Category category) { this.category = category; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public Product build() {
            return new Product(id, name, description, price, category, active);
        }
    }
}
