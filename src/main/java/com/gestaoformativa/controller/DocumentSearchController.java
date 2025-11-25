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

@RestController
@RequestMapping("/api/search")
public class DocumentSearchController {

    @Autowired
    private DocumentSearchService searchService;

    @Autowired
    private UserService userService;

    @GetMapping("/documents")
    public ResponseEntity<Page<DocumentDTO>> searchDocuments(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) FormativeDocument.DocumentType documentType,
            @RequestParam(required = false) FormativeDocument.AccessLevel accessLevel,
            @RequestParam(required = false) User.LifeStage stage,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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

    // Busca por texto no conteúdo dos documentos
    @GetMapping("/content")
    public ResponseEntity<List<DocumentDTO>> searchByContent(
            @RequestParam String text,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        List<FormativeDocument> documents = searchService.searchByContent(text);

        // Filtrar documentos que o usuário pode acessar e converter para DTOs
        List<DocumentDTO> documentDTOs = documents.stream()
                .filter(currentUser::canAccessDocument)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documentDTOs);
    }

    // Obter documentos recentemente atualizados
    @GetMapping("/recent")
    public ResponseEntity<List<DocumentDTO>> getRecentlyUpdatedDocuments(
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        List<FormativeDocument> documents = searchService.getRecentlyUpdatedDocuments();

        // Filtrar documentos que o usuário pode acessar e converter para DTOs
        List<DocumentDTO> documentDTOs = documents.stream()
                .filter(currentUser::canAccessDocument)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documentDTOs);
    }

    // Obter documentos mais visualizados
    @GetMapping("/most-viewed")
    public ResponseEntity<List<DocumentDTO>> getMostViewedDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        List<FormativeDocument> documents = searchService.getMostViewedDocuments(pageable);

        // Filtrar documentos que o usuário pode acessar e converter para DTOs
        List<DocumentDTO> documentDTOs = documents.stream()
                .filter(currentUser::canAccessDocument)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documentDTOs);
    }

    // Obter documentos recomendados para o usuário atual
    @GetMapping("/recommended")
    public ResponseEntity<List<DocumentDTO>> getRecommendedDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        List<FormativeDocument> documents = searchService.getRecommendedDocumentsForUser(currentUser.getId(), pageable);

        // Converter para DTOs
        List<DocumentDTO> documentDTOs = documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documentDTOs);
    }

    // Método auxiliar
    private DocumentDTO convertToDTO(FormativeDocument document) {
        DocumentDTO dto = new DocumentDTO();
        // Preencher o DTO com os dados do documento
        // ...
        return dto;
    }
}