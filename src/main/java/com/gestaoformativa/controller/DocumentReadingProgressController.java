package com.gestaoformativa.controller;

import com.gestaoformativa.dto.ReadingProgressDTO;
import com.gestaoformativa.model.DocumentReadingProgress;
import com.gestaoformativa.model.FormativeDocument;
import com.gestaoformativa.model.User;
import com.gestaoformativa.service.DocumentReadingProgressService;
import com.gestaoformativa.service.FormativeDocumentService;
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
@RequestMapping("/api/reading-progress")
@Tag(name = "Progresso de Leitura", description = "Gerenciamento do progresso de leitura de documentos")
@SecurityRequirement(name = "bearer-jwt")
public class DocumentReadingProgressController {

    @Autowired
    private DocumentReadingProgressService progressService;

    @Autowired
    private FormativeDocumentService documentService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Obter progresso do usuário", description = "Retorna o progresso de leitura do usuário atual para um documento específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progresso encontrado"),
            @ApiResponse(responseCode = "204", description = "Nenhum progresso encontrado"),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    })
    @GetMapping("/document/{documentId}")
    public ResponseEntity<ReadingProgressDTO> getUserDocumentProgress(@PathVariable Long documentId,
                                                                      @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            DocumentReadingProgress progress = progressService.getReadingProgress(currentUser.getId(), documentId);

            if (progress == null) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(convertToDTO(progress));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Atualizar progresso", description = "Atualiza ou cria o progresso de leitura do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progresso atualizado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado ao documento"),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    })
    @PostMapping("/document/{documentId}")
    public ResponseEntity<ReadingProgressDTO> updateReadingProgress(@PathVariable Long documentId,
                                                                    @Valid @RequestBody ReadingProgressDTO progressDTO,
                                                                    @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            FormativeDocument document = documentService.getDocumentById(documentId);
            if (!currentUser.canAccessDocument(document)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            DocumentReadingProgress progress = progressService.updateReadingProgress(
                    currentUser.getId(),
                    documentId,
                    progressDTO.getProgressPercentage(),
                    progressDTO.getUserNotes()
            );

            return ResponseEntity.ok(convertToDTO(progress));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "Documentos concluídos", description = "Retorna todos os documentos concluídos pelo usuário")
    @ApiResponse(responseCode = "200", description = "Lista de documentos concluídos")
    @GetMapping("/completed")
    public ResponseEntity<List<ReadingProgressDTO>> getCompletedDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        List<DocumentReadingProgress> completedProgress = progressService.getCompletedDocumentsByUser(currentUser.getId());

        List<ReadingProgressDTO> progressDTOs = completedProgress.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(progressDTOs);
    }

    @Operation(summary = "Documentos em andamento", description = "Retorna documentos não concluídos pelo usuário")
    @ApiResponse(responseCode = "200", description = "Lista de documentos em andamento")
    @GetMapping("/in-progress")
    public ResponseEntity<List<ReadingProgressDTO>> getInProgressDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        List<DocumentReadingProgress> inProgressDocs = progressService.getInProgressDocumentsByUser(currentUser.getId());

        List<ReadingProgressDTO> progressDTOs = inProgressDocs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(progressDTOs);
    }

    @Operation(summary = "Documentos recentes", description = "Retorna documentos recentemente visualizados pelo usuário")
    @ApiResponse(responseCode = "200", description = "Lista de documentos recentes")
    @GetMapping("/recent")
    public ResponseEntity<List<ReadingProgressDTO>> getRecentlyViewedDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        List<FormativeDocument> recentDocs = progressService.getRecentlyViewedDocuments(currentUser.getId());

        List<ReadingProgressDTO> progressDTOs = recentDocs.stream()
                .map(doc -> {
                    DocumentReadingProgress progress = progressService.getReadingProgress(currentUser.getId(), doc.getId());
                    return convertToDTO(progress);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(progressDTOs);
    }

    @Operation(summary = "Resetar progresso", description = "Reseta o progresso de leitura para um documento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Progresso resetado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    })
    @DeleteMapping("/document/{documentId}")
    public ResponseEntity<Void> resetReadingProgress(@PathVariable Long documentId,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            documentService.getDocumentById(documentId);
            progressService.resetReadingProgress(currentUser.getId(), documentId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private ReadingProgressDTO convertToDTO(DocumentReadingProgress progress) {
        ReadingProgressDTO dto = new ReadingProgressDTO();
        dto.setId(progress.getId());
        dto.setUserId(progress.getUser().getId());
        dto.setDocumentId(progress.getDocument().getId());
        dto.setProgressPercentage(progress.getProgressPercentage());
        dto.setUserNotes(progress.getUserNotes());
        return dto;
    }
}