package com.fiap.techchallenge.application.usecases;

import com.fiap.techchallenge.domain.entities.Category;
import com.fiap.techchallenge.domain.entities.Product;
import com.fiap.techchallenge.domain.exception.DomainException;
import com.fiap.techchallenge.domain.exception.NotFoundException;
import com.fiap.techchallenge.domain.repositories.CategoryRepository;
import com.fiap.techchallenge.domain.repositories.ProductRepository;
import com.fiap.techchallenge.infrastructure.logging.LogCategory;
import com.fiap.techchallenge.infrastructure.logging.StructuredLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryUseCaseImpl implements CategoryUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CategoryUseCaseImpl.class);
    private static final String RECORD_NOT_FOUND_MESSAGE = "Record not found";

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryUseCaseImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Category createCategory(String name) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("CreateCategory");
            StructuredLogger.put("categoryName", name);
            
            logger.info("Category creation started: name={}", name);
            
            if (categoryRepository.existsByName(name)) {
                logger.warn("Category creation failed - name already exists: name={}", name);
                throw new DomainException("Category with name " + name + " already exists");
            }

            Category category = Category.builder()
                    .id(UUID.randomUUID())
                    .name(name)
                    .build();

            Category savedCategory = categoryRepository.save(category);
            logger.info("Category created successfully: categoryId={}, name={}", 
                       savedCategory.getId(), name);
            
            return savedCategory;
            
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("CATEGORY_CREATION_FAILED", e.getMessage());
            logger.error("Failed to create category: name={}", name, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public Category updateCategory(UUID id, String name) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("UpdateCategory");
            StructuredLogger.put("categoryId", id.toString());
            StructuredLogger.put("categoryName", name);
            
            logger.info("Category update started: categoryId={}, newName={}", id, name);
            
            if (name == null || name.trim().isEmpty()) {
                logger.warn("Category update failed - name is blank: categoryId={}", id);
                throw new DomainException("Category name cannot be blank.");
            }

            Category updatedCategory = categoryRepository.findById(id)
                    .map(category -> {
                        Category updated = Category.builder()
                                .id(category.getId())
                                .name(name)
                                .build();
                        return categoryRepository.save(updated);
                    })
                    .orElseThrow(() -> {
                        logger.warn("Category update failed - not found: categoryId={}", id);
                        return new NotFoundException(RECORD_NOT_FOUND_MESSAGE);
                    });
            
            logger.info("Category updated successfully: categoryId={}, name={}", id, name);
            return updatedCategory;
            
        } catch (NotFoundException e) {
            throw e;
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("CATEGORY_UPDATE_FAILED", e.getMessage());
            logger.error("Failed to update category: categoryId={}", id, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public Optional<Category> findById(UUID id) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("FindCategoryById");
            StructuredLogger.put("categoryId", id.toString());
            
            Optional<Category> category = categoryRepository.findById(id);
            if (category.isEmpty()) {
                logger.warn("Category not found: categoryId={}", id);
                throw new NotFoundException(RECORD_NOT_FOUND_MESSAGE);
            }
            
            logger.info("Category found: categoryId={}, name={}", id, category.get().getName());
            return category;
            
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("CATEGORY_FIND_FAILED", e.getMessage());
            logger.error("Failed to find category: categoryId={}", id, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public List<Category> findAll() {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("FindAllCategories");
            
            List<Category> categories = categoryRepository.findAll();
            logger.info("Categories listed: count={}", categories.size());
            
            return categories;
            
        } catch (Exception e) {
            StructuredLogger.setError("CATEGORY_LIST_FAILED", e.getMessage());
            logger.error("Failed to list categories", e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }

    @Override
    public void deleteById(UUID id) {
        try {
            StructuredLogger.setCategory(LogCategory.BUSINESS);
            StructuredLogger.setOperation("DeleteCategory");
            StructuredLogger.put("categoryId", id.toString());
            
            logger.info("Category deletion started: categoryId={}", id);
            
            if (categoryRepository.findById(id).isEmpty()) {
                logger.warn("Category deletion failed - not found: categoryId={}", id);
                throw new NotFoundException(RECORD_NOT_FOUND_MESSAGE);
            }

            // Verifica se a categoria está vinculada a produtos
            List<Product> productsInCategory = productRepository.findByCategoryId(id);
            if (!productsInCategory.isEmpty()) {
                logger.warn("Category deletion failed - linked to products: categoryId={}, productsCount={}", 
                           id, productsInCategory.size());
                throw new DomainException("Não é possível deletar a categoria pois ela está vinculada a um ou mais produtos");
            }

            categoryRepository.deleteById(id);
            logger.info("Category deleted successfully: categoryId={}", id);
            
        } catch (NotFoundException e) {
            throw e;
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            StructuredLogger.setError("CATEGORY_DELETION_FAILED", e.getMessage());
            logger.error("Failed to delete category: categoryId={}", id, e);
            throw e;
        } finally {
            StructuredLogger.clear();
        }
    }
}
