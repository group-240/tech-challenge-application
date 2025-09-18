package com.fiap.techchallenge.external.api;

import com.fiap.techchallenge.domain.entities.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryTest {

    @Test
    @DisplayName("Deve criar uma categoria usando o construtor")
    public void shouldCreateCategoryUsingConstructor() {
        UUID id = UUID.randomUUID();
        String name = "Electronics";

        Category category = new Category(id, name);

        assertEquals(id, category.getId());
        assertEquals(name, category.getName());
    }

    @Test
    @DisplayName("Deve criar uma categoria usando os setters")
    public void shouldCreateCategoryUsingSetters() {
        UUID id = UUID.randomUUID();
        String name = "Books";

        Category category = new Category();
        category.setId(id);
        category.setName(name);

        assertEquals(id, category.getId());
        assertEquals(name, category.getName());
    }

    @Test
    @DisplayName("Deve criar uma categoria usando o Builder")
    public void shouldCreateCategoryUsingBuilder() {
        UUID id = UUID.randomUUID();
        String name = "Clothing";

        Category category = Category.builder()
                .id(id)
                .name(name)
                .build();

        assertEquals(id, category.getId());
        assertEquals(name, category.getName());
    }
}