package com.gestaoformativa.controller;

import com.gestaoformativa.dto.CategoryDTO;
import com.gestaoformativa.model.DocumentCategory;
import com.gestaoformativa.model.User;
import com.gestaoformativa.service.DocumentCategoryService;
import com.gestaoformativa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class DocumentCategoryController {

    @Autowired
    private DocumentCategoryService categoryService;

    @Autowired
    private UserService userService;

    // Obter todas as categorias
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<DocumentCategory> categories = categoryService.getAllCategories();

        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryDTOs);
    }

    // Obter categorias de nível superior (raiz)
    @GetMapping("/root")
    public ResponseEntity<List<CategoryDTO>> getRootCategories() {
        List<DocumentCategory> rootCategories = categoryService.getRootCategories();

        List<CategoryDTO> categoryDTOs = rootCategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryDTOs);
    }

    // Obter uma categoria específica por ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        try {
            DocumentCategory category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(convertToDTO(category));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Obter subcategorias de uma categoria
    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<CategoryDTO>> getSubcategories(@PathVariable Long id) {
        try {
            List<DocumentCategory> subcategories = categoryService.getSubcategories(id);

            List<CategoryDTO> categoryDTOs = subcategories.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(categoryDTOs);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Criar uma nova categoria
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar documentos
        if (!currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DocumentCategory category = convertToEntity(categoryDTO);

        // Verificar se existe uma categoria pai
        if (categoryDTO.getParentCategoryId() != null) {
            try {
                DocumentCategory parentCategory = categoryService.getCategoryById(categoryDTO.getParentCategoryId());
                category.setParentCategory(parentCategory);
            } catch (EntityNotFoundException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        DocumentCategory savedCategory = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedCategory));
    }

    // Atualizar uma categoria existente
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id,
                                                      @Valid @RequestBody CategoryDTO categoryDTO,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar documentos
        if (!currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            DocumentCategory category = convertToEntity(categoryDTO);

            // Verificar se existe uma categoria pai
            if (categoryDTO.getParentCategoryId() != null) {
                try {
                    DocumentCategory parentCategory = categoryService.getCategoryById(categoryDTO.getParentCategoryId());
                    category.setParentCategory(parentCategory);
                } catch (EntityNotFoundException e) {
                    return ResponseEntity.badRequest().build();
                }
            }

            DocumentCategory updatedCategory = categoryService.updateCategory(id, category);
            return ResponseEntity.ok(convertToDTO(updatedCategory));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Excluir uma categoria
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar documentos
        if (!currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // Adicionar um documento a uma categoria
    @PostMapping("/{categoryId}/documents/{documentId}")
    public ResponseEntity<CategoryDTO> addDocumentToCategory(@PathVariable Long categoryId,
                                                             @PathVariable Long documentId,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar documentos
        if (!currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            DocumentCategory updatedCategory = categoryService.addDocumentToCategory(categoryId, documentId);
            return ResponseEntity.ok(convertToDTO(updatedCategory));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Remover um documento de uma categoria
    @DeleteMapping("/{categoryId}/documents/{documentId}")
    public ResponseEntity<CategoryDTO> removeDocumentFromCategory(@PathVariable Long categoryId,
                                                                  @PathVariable Long documentId,
                                                                  @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar documentos
        if (!currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            DocumentCategory updatedCategory = categoryService.removeDocumentFromCategory(categoryId, documentId);
            return ResponseEntity.ok(convertToDTO(updatedCategory));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Métodos auxiliares
    private CategoryDTO convertToDTO(DocumentCategory category) {
        CategoryDTO dto = new CategoryDTO();
        // Preencher o DTO com os dados da categoria
        // ...
        return dto;
    }

    private DocumentCategory convertToEntity(CategoryDTO dto) {
        DocumentCategory category = new DocumentCategory();
        // Preencher a entidade com os dados do DTO
        // ...
        return category;
    }
}