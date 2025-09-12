package com.fiap.techchallenge.external.api;

import com.fiap.techchallenge.adapters.controllers.ProductController;
import com.fiap.techchallenge.domain.entities.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "API para gerenciamento de produtos")
public class ProductRestController {

    private final ProductController productController;

    public ProductRestController(ProductController productController) {
        this.productController = productController;
    }

    @PostMapping
    @Operation(summary = "Criar novo produto")
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequestDTO productRequest) {
        Product product = productController.createProduct(
                productRequest.getName(),
                productRequest.getDescription(),
                productRequest.getPrice(),
                productRequest.getCategoryId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<Product> findProductById(@PathVariable UUID id) {
        return productController.findProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar todos os produtos")
    public ResponseEntity<List<Product>> findAllProducts() {
        List<Product> products = productController.findAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar produtos por nome")
    public ResponseEntity<List<Product>> findProductsByName(@RequestParam String name) {
        List<Product> products = productController.findProductsByName(name);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Buscar produtos por categoria")
    public ResponseEntity<List<Product>> findProductsByCategory(@PathVariable UUID categoryId) {
        List<Product> products = productController.findProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto")
    public ResponseEntity<Product> updateProduct(@PathVariable UUID id, @RequestBody ProductRequestDTO productRequest) {
        Product product = productController.updateProduct(
                id,
                productRequest.getName(),
                productRequest.getDescription(),
                productRequest.getPrice(),
                productRequest.getCategoryId()
        );
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar produto")
    public ResponseEntity<String> deleteProduct(@PathVariable UUID id) {
        productController.deleteProduct(id);
        return ResponseEntity.ok("Produto deletado com sucesso.");
    }

    // DTO interno
    public static class ProductRequestDTO {
        private String name;
        private String description;
        private BigDecimal price;
        private UUID categoryId;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public UUID getCategoryId() { return categoryId; }
        public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    }
}
