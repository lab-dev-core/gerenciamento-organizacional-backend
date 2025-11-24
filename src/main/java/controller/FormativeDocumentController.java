package controller;

import dto.DocumentDTO;
import model.FormativeDocument;
import model.MissionLocation;
import model.User;
import service.FormativeDocumentService;
import service.MissionLocationService;
import service.RoleService;
import service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class FormativeDocumentController {

    @Autowired
    private FormativeDocumentService documentService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MissionLocationService locationService;

    // Obter todos os documentos que o usuário atual pode acessar
    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getAccessibleDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        List<FormativeDocument> documents = documentService.getAccessibleDocuments(currentUser);

        List<DocumentDTO> documentDTOs = documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documentDTOs);
    }

    // Obter um documento específico
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable Long id,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        FormativeDocument document = documentService.getDocumentById(id);

        // Verificar se o usuário tem acesso ao documento
        if (!currentUser.canAccessDocument(document)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(convertToDTO(document));
    }

    // Criar um novo documento
    @PostMapping
    public ResponseEntity<DocumentDTO> createDocument(@Valid @RequestBody DocumentDTO documentDTO,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para criar documentos
        if (!currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        FormativeDocument document = convertToEntity(documentDTO);
        FormativeDocument savedDocument = documentService.createDocument(document, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedDocument));
    }

    // Atualizar um documento existente
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDTO> updateDocument(@PathVariable Long id,
                                                      @Valid @RequestBody DocumentDTO documentDTO,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        FormativeDocument existingDocument = documentService.getDocumentById(id);

        // Verificar se o usuário é o autor ou tem permissão para gerenciar documentos
        if (!existingDocument.getAuthor().equals(currentUser) && !currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        FormativeDocument document = convertToEntity(documentDTO);
        document.setId(id);
        FormativeDocument updatedDocument = documentService.updateDocument(id, document);

        return ResponseEntity.ok(convertToDTO(updatedDocument));
    }

    // Excluir um documento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        FormativeDocument document = documentService.getDocumentById(id);

        // Verificar se o usuário é o autor ou tem permissão para gerenciar documentos
        if (!document.getAuthor().equals(currentUser) && !currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    // Fazer upload de um anexo para o documento
    @PostMapping("/{id}/attachment")
    public ResponseEntity<DocumentDTO> uploadAttachment(@PathVariable Long id,
                                                        @RequestParam("file") MultipartFile file,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        FormativeDocument document = documentService.getDocumentById(id);

        // Verificar se o usuário é o autor ou tem permissão para gerenciar documentos
        if (!document.getAuthor().equals(currentUser) && !currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            document.setAttachmentData(file.getBytes());
            document.setAttachmentName(file.getOriginalFilename());
            document.setAttachmentType(file.getContentType());

            FormativeDocument updatedDocument = documentService.updateDocument(id, document);
            return ResponseEntity.ok(convertToDTO(updatedDocument));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obter documentos específicos para uma etapa
    @GetMapping("/by-stage/{stageName}")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByStage(@PathVariable String stageName,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            User.LifeStage stage = User.LifeStage.valueOf(stageName);
            List<FormativeDocument> documents = documentService.getDocumentsForStage(stage);

            // Filtrar apenas os documentos que o usuário pode acessar
            List<DocumentDTO> accessibleDocuments = documents.stream()
                    .filter(currentUser::canAccessDocument)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(accessibleDocuments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obter documentos específicos para um local
    @GetMapping("/by-location/{locationId}")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByLocation(@PathVariable Long locationId,
                                                                    @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        try {
            MissionLocation location = locationService.getLocationById(locationId);
            List<FormativeDocument> documents = documentService.getDocumentsForLocation(location);

            // Filtrar apenas os documentos que o usuário pode acessar
            List<DocumentDTO> accessibleDocuments = documents.stream()
                    .filter(currentUser::canAccessDocument)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(accessibleDocuments);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Conceder acesso a um documento para um usuário
    @PostMapping("/{documentId}/access/user/{userId}")
    public ResponseEntity<DocumentDTO> grantAccessToUser(@PathVariable Long documentId,
                                                         @PathVariable Long userId,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        FormativeDocument document = documentService.getDocumentById(documentId);

        // Verificar se o usuário é o autor ou tem permissão para gerenciar documentos
        if (!document.getAuthor().equals(currentUser) && !currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            User targetUser = userService.getUserById(userId);
            FormativeDocument updatedDocument = documentService.grantAccessToUser(document, targetUser);
            return ResponseEntity.ok(convertToDTO(updatedDocument));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Métodos auxiliares
    private DocumentDTO convertToDTO(FormativeDocument document) {
        DocumentDTO dto = new DocumentDTO();
        // Preencher o DTO com os dados do documento
        // ...
        return dto;
    }

    private FormativeDocument convertToEntity(DocumentDTO dto) {
        FormativeDocument document = new FormativeDocument();
        // Preencher a entidade com os dados do DTO
        // ...
        return document;
    }
}