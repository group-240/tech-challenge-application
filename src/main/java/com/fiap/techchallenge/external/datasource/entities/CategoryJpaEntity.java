package com.fiap.techchallenge.external.datasource.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class CategoryJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    public CategoryJpaEntity() {}

    public CategoryJpaEntity(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
