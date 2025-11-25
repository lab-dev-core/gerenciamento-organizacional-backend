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

@RestController
@RequestMapping("/api/reading-progress")
public class DocumentReadingProgressController {

    @Autowired
    private DocumentReadingProgressService progressService;

    @Autowired
    private FormativeDocumentService documentService;

    @Autowired
    private UserService userService;

    // Obter progresso de leitura para o usuário atual e documento específico
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

    // Atualizar progresso de leitura
    @PostMapping("/document/{documentId}")
    public ResponseEntity<ReadingProgressDTO> updateReadingProgress(@PathVariable Long documentId,
                                                                    @Valid @RequestBody ReadingProgressDTO progressDTO,
                                                                    @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            // Verificar se o documento existe e se o usuário pode acessá-lo
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

    // Obter todos os documentos concluídos pelo usuário atual
    @GetMapping("/completed")
    public ResponseEntity<List<ReadingProgressDTO>> getCompletedDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        List<DocumentReadingProgress> completedProgress = progressService.getCompletedDocumentsByUser(currentUser.getId());

        List<ReadingProgressDTO> progressDTOs = completedProgress.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(progressDTOs);
    }

    // Obter todos os documentos em andamento (não concluídos) pelo usuário atual
    @GetMapping("/in-progress")
    public ResponseEntity<List<ReadingProgressDTO>> getInProgressDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        List<DocumentReadingProgress> inProgressDocs = progressService.getInProgressDocumentsByUser(currentUser.getId());

        List<ReadingProgressDTO> progressDTOs = inProgressDocs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(progressDTOs);
    }

    // Obter documentos recentemente visualizados pelo usuário atual
    @GetMapping("/recent")
    public ResponseEntity<List<ReadingProgressDTO>> getRecentlyViewedDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        List<FormativeDocument> recentDocs = progressService.getRecentlyViewedDocuments(currentUser.getId());

        // Converter para DTOs de progresso
        List<ReadingProgressDTO> progressDTOs = recentDocs.stream()
                .map(doc -> {
                    DocumentReadingProgress progress = progressService.getReadingProgress(currentUser.getId(), doc.getId());
                    return convertToDTO(progress);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(progressDTOs);
    }

    // Reset de progresso de leitura para um documento específico
    @DeleteMapping("/document/{documentId}")
    public ResponseEntity<Void> resetReadingProgress(@PathVariable Long documentId,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            // Verificar se o documento existe
            documentService.getDocumentById(documentId);

            progressService.resetReadingProgress(currentUser.getId(), documentId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Método auxiliar
    private ReadingProgressDTO convertToDTO(DocumentReadingProgress progress) {
        ReadingProgressDTO dto = new ReadingProgressDTO();
        // Preencher o DTO com os dados do progresso
        // ...
        return dto;
    }
}