package com.gestaoformativa.controller;

import com.gestaoformativa.dto.DocumentDTO;
import com.gestaoformativa.model.FormativeDocument;
import com.gestaoformativa.model.User;
import org.springframework.data.domain.PageImpl;
import com.gestaoformativa.service.DocumentSearchService;
import com.gestaoformativa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Busca de Documentos", description = "Endpoints para busca e filtragem de documentos")
@SecurityRequirement(name = "bearer-jwt")
public class DocumentSearchController {

    @Autowired
    private DocumentSearchService searchService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Buscar documentos", description = "Busca documentos com múltiplos critérios de filtro")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @GetMapping("/documents")
    public ResponseEntity<Page<DocumentDTO>> searchDocuments(
            @Parameter(description = "Título do documento") @RequestParam(required = false) String title,
            @Parameter(description = "ID do autor") @RequestParam(required = false) Long authorId,
            @Parameter(description = "Tipo de documento") @RequestParam(required = false) FormativeDocument.DocumentType documentType,
            @Parameter(description = "Nível de acesso") @RequestParam(required = false) FormativeDocument.AccessLevel accessLevel,
            @Parameter(description = "Estágio de vida") @RequestParam(required = false) User.LifeStage stage,
            @Parameter(description = "ID da localização") @RequestParam(required = false) Long locationId,
            @Parameter(description = "Data inicial") @RequestParam(required = false) LocalDateTime fromDate,
            @Parameter(description = "Data final") @RequestParam(required = false) LocalDateTime toDate,
            @Parameter(description = "Palavra-chave") @RequestParam(required = false) String keyword,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);

        Page<FormativeDocument> documents = searchService.searchDocuments(
                title, authorId, documentType, accessLevel, stage,
                locationId, fromDate, toDate, keyword, pageable);

        List<DocumentDTO> content = documents.getContent().stream()
                .filter(currentUser::canAccessDocument)
                .map(this::convertToDTO)
                .toList();

        Page<DocumentDTO> dtoPage = new PageImpl<>(content, pageable, content.size());

        return ResponseEntity.ok(dtoPage);
    }

    @Operation(summary = "Buscar por conteúdo", description = "Busca documentos pelo conteúdo textual")
    @ApiResponse(responseCode = "200", description = "Busca por conteúdo realizada com sucesso")
    @GetMapping("/content")
    public ResponseEntity<List<DocumentDTO>> searchByContent(
            @Parameter(description = "Texto para busca", required = true) @RequestParam String text,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        List<FormativeDocument> documents = searchService.searchByContent(text);

        List<DocumentDTO> documentDTOs = documents.stream()
                .filter(currentUser::canAccessDocument)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documentDTOs);
    }

    @Operation(summary = "Documentos recentes", description = "Retorna documentos recentemente atualizados")
    @ApiResponse(responseCode = "200", description = "Documentos recentes listados com sucesso")
    @GetMapping("/recent")
    public ResponseEntity<List<DocumentDTO>> getRecentlyUpdatedDocuments(
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        List<FormativeDocument> documents = searchService.getRecentlyUpdatedDocuments();

        List<DocumentDTO> documentDTOs = documents.stream()
                .filter(currentUser::canAccessDocument)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documentDTOs);
    }

    @Operation(summary = "Documentos mais visualizados", description = "Retorna os documentos mais visualizados")
    @ApiResponse(responseCode = "200", description = "Documentos mais visualizados listados com sucesso")
    @GetMapping("/most-viewed")
    public ResponseEntity<List<DocumentDTO>> getMostViewedDocuments(
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        List<FormativeDocument> documents = searchService.getMostViewedDocuments(pageable);

        List<DocumentDTO> documentDTOs = documents.stream()
                .filter(currentUser::canAccessDocument)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documentDTOs);
    }

    @Operation(summary = "Documentos recomendados", description = "Retorna documentos recomendados para o usuário")
    @ApiResponse(responseCode = "200", description = "Documentos recomendados listados com sucesso")
    @GetMapping("/recommended")
    public ResponseEntity<List<DocumentDTO>> getRecommendedDocuments(
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        List<FormativeDocument> documents = searchService.getRecommendedDocumentsForUser(currentUser.getId(), pageable);

        List<DocumentDTO> documentDTOs = documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documentDTOs);
    }

    private DocumentDTO convertToDTO(FormativeDocument document) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setTitle(document.getTitle());
        dto.setContent(document.getContent());
        dto.setKeywords(document.getKeywords());
        dto.setDocumentType(document.getDocumentType());
        dto.setAccessLevel(document.getAccessLevel());
        // Preencher outros campos necessários
        return dto;
    }
}