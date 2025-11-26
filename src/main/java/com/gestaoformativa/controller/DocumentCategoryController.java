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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categorias de Documentos", description = "Gerenciamento de categorias de documentos")
@SecurityRequirement(name = "bearer-jwt")
public class DocumentCategoryController {

    @Autowired
    private DocumentCategoryService categoryService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Listar todas as categorias", description = "Retorna todas as categorias de documentos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorias listadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<DocumentCategory> categories = categoryService.getAllCategories();

        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryDTOs);
    }

    @Operation(summary = "Obter categorias raiz", description = "Retorna apenas categorias de nível superior (sem pai)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorias raiz listadas com sucesso")
    })
    @GetMapping("/root")
    public ResponseEntity<List<CategoryDTO>> getRootCategories() {
        List<DocumentCategory> rootCategories = categoryService.getRootCategories();

        List<CategoryDTO> categoryDTOs = rootCategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryDTOs);
    }

    @Operation(summary = "Obter categoria por ID", description = "Retorna uma categoria específica pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        try {
            DocumentCategory category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(convertToDTO(category));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Obter subcategorias", description = "Retorna todas as subcategorias de uma categoria pai")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subcategorias listadas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria pai não encontrada")
    })
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

    @Operation(summary = "Criar nova categoria", description = "Cria uma nova categoria de documento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar documentos")
    })
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DocumentCategory category = convertToEntity(categoryDTO);

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

    @Operation(summary = "Atualizar categoria", description = "Atualiza uma categoria existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar documentos"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id,
                                                      @Valid @RequestBody CategoryDTO categoryDTO,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            DocumentCategory category = convertToEntity(categoryDTO);

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

    @Operation(summary = "Excluir categoria", description = "Exclui uma categoria existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria excluída com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar documentos"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflito - categoria não pode ser excluída")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

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

    @Operation(summary = "Adicionar documento à categoria", description = "Associa um documento a uma categoria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento adicionado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar documentos"),
            @ApiResponse(responseCode = "404", description = "Categoria ou documento não encontrado")
    })
    @PostMapping("/{categoryId}/documents/{documentId}")
    public ResponseEntity<CategoryDTO> addDocumentToCategory(@PathVariable Long categoryId,
                                                             @PathVariable Long documentId,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

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

    @Operation(summary = "Remover documento da categoria", description = "Remove a associação de um documento com uma categoria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento removido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar documentos"),
            @ApiResponse(responseCode = "404", description = "Categoria ou documento não encontrado")
    })
    @DeleteMapping("/{categoryId}/documents/{documentId}")
    public ResponseEntity<CategoryDTO> removeDocumentFromCategory(@PathVariable Long categoryId,
                                                                  @PathVariable Long documentId,
                                                                  @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

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

    private CategoryDTO convertToDTO(DocumentCategory category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        if (category.getParentCategory() != null) {
            dto.setParentCategoryId(category.getParentCategory().getId());
        }
        return dto;
    }

    private DocumentCategory convertToEntity(CategoryDTO dto) {
        DocumentCategory category = new DocumentCategory();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }
}