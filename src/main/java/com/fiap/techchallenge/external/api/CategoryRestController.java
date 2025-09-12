package com.fiap.techchallenge.external.api;

import com.fiap.techchallenge.adapters.controllers.CategoryController;
import com.fiap.techchallenge.domain.entities.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categories", description = "API para gerenciamento de categorias")
public class CategoryRestController {

    private final CategoryController categoryController;

    public CategoryRestController(CategoryController categoryController) {
        this.categoryController = categoryController;
    }

    @PostMapping
    @Operation(summary = "Criar nova categoria")
    public ResponseEntity<Category> createCategory(@RequestBody CategoryRequestDTO categoryRequest) {
        Category category = categoryController.createCategory(categoryRequest.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    public ResponseEntity<Category> findById(@PathVariable UUID id) {
        return categoryController.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar todas as categorias")
    public ResponseEntity<List<Category>> findAll() {
        List<Category> categories = categoryController.findAll();
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria")
    public ResponseEntity<Category> updateCategory(@PathVariable UUID id, @RequestBody CategoryRequestDTO categoryRequest) {
        Category category = categoryController.updateCategory(id, categoryRequest.getName());
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar categoria")
    public ResponseEntity<String> deleteById(@PathVariable UUID id) {
        categoryController.deleteById(id);
        return ResponseEntity.ok("Categoria deletada com sucesso.");
    }

    // DTO interno
    public static class CategoryRequestDTO {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
