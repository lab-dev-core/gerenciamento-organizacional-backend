package com.gestaoformativa.controller;

import com.gestaoformativa.dto.DocumentDTO;
import com.gestaoformativa.model.FormativeDocument;
import com.gestaoformativa.model.MissionLocation;
import com.gestaoformativa.model.Role;
import com.gestaoformativa.model.User;
import com.gestaoformativa.service.FormativeDocumentService;
import com.gestaoformativa.service.MissionLocationService;
import com.gestaoformativa.service.RoleService;
import com.gestaoformativa.service.UserService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getAccessibleDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        List<FormativeDocument> documents = documentService.getAccessibleDocuments(currentUser);

        List<DocumentDTO> documentDTOs = documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documentDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable Long id,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        FormativeDocument document = documentService.getDocumentById(id);

        if (!currentUser.canAccessDocument(document)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(convertToDTO(document));
    }

    @PostMapping
    public ResponseEntity<DocumentDTO> createDocument(@Valid @RequestBody DocumentDTO documentDTO,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        FormativeDocument document = convertToEntity(documentDTO);
        FormativeDocument savedDocument = documentService.createDocument(document, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedDocument));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentDTO> updateDocument(@PathVariable Long id,
                                                      @Valid @RequestBody DocumentDTO documentDTO,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        FormativeDocument existingDocument = documentService.getDocumentById(id);

        if (!existingDocument.getAuthor().equals(currentUser) && !currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        FormativeDocument document = convertToEntity(documentDTO);
        document.setId(id);
        FormativeDocument updatedDocument = documentService.updateDocument(id, document);

        return ResponseEntity.ok(convertToDTO(updatedDocument));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        FormativeDocument document = documentService.getDocumentById(id);

        if (!document.getAuthor().equals(currentUser) && !currentUser.hasPermission("documents")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/attachment")
    public ResponseEntity<DocumentDTO> uploadAttachment(@PathVariable Long id,
                                                        @RequestParam("file") MultipartFile file,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        FormativeDocument document = documentService.getDocumentById(id);

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

    @PostMapping("/{documentId}/access/user/{userId}")
    public ResponseEntity<DocumentDTO> grantAccessToUser(@PathVariable Long documentId,
                                                         @PathVariable Long userId,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        FormativeDocument document = documentService.getDocumentById(documentId);

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

    private DocumentDTO convertToDTO(FormativeDocument document) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setTitle(document.getTitle());
        dto.setContent(document.getContent());
        dto.setKeywords(document.getKeywords());
        dto.setDocumentType(document.getDocumentType());
        dto.setAccessLevel(document.getAccessLevel());
        return dto;
    }

    private FormativeDocument convertToEntity(DocumentDTO dto) {
        FormativeDocument document = new FormativeDocument();

        document.setTitle(dto.getTitle());
        document.setContent(dto.getContent());
        document.setKeywords(dto.getKeywords());

        if (dto.getDocumentType() != null) {
            document.setDocumentType(
                    FormativeDocument.DocumentType.valueOf(String.valueOf(dto.getDocumentType()))
            );
        }

        if (dto.getAccessLevel() != null) {
            document.setAccessLevel(
                    FormativeDocument.AccessLevel.valueOf(String.valueOf(dto.getAccessLevel()))
            );
        }

        if (dto.getAllowedStages() != null && !dto.getAllowedStages().isEmpty()) {
            Set<User.LifeStage> stages = new HashSet<>();
            for (String stageName : dto.getAllowedStages()) {
                try {
                    stages.add(User.LifeStage.valueOf(stageName));
                } catch (IllegalArgumentException e) {
                    // Log ou ignorar estágios inválidos
                }
            }
            document.setAllowedStages(stages);
        }

        if (dto.getAllowedLocationIds() != null && !dto.getAllowedLocationIds().isEmpty()) {
            Set<MissionLocation> locations = new HashSet<>();
            for (Long locationId : dto.getAllowedLocationIds()) {
                try {
                    MissionLocation location = locationService.getLocationById(locationId);
                    locations.add(location);
                } catch (Exception e) {
                    // Log ou ignorar locais inválidos
                }
            }
            document.setAllowedLocations(locations);
        }

        if (dto.getAllowedUserIds() != null && !dto.getAllowedUserIds().isEmpty()) {
            Set<User> users = new HashSet<>();
            for (Long userId : dto.getAllowedUserIds()) {
                try {
                    User user = userService.getUserById(userId);
                    users.add(user);
                } catch (Exception e) {
                    // Log ou ignorar usuários inválidos
                }
            }
            document.setAllowedUsers(users);
        }

        if (dto.getAllowedRoleIds() != null && !dto.getAllowedRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : dto.getAllowedRoleIds()) {
                try {
                    Role role = roleService.getRoleById(roleId);
                    roles.add(role);
                } catch (Exception e) {
                    // Log ou ignorar roles inválidas
                }
            }
            document.setAllowedRoles(roles);
        }

        return document;
    }
}