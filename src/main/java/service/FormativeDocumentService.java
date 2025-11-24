package service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import model.*;
import repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FormativeDocumentService {

    @Autowired
    private FormativeDocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentReadingProgressRepository progressRepository;

    // Criar um novo documento
    public FormativeDocument createDocument(FormativeDocument document, User author) {
        document.setAuthor(author);
        document.setCreationDate(LocalDateTime.now());
        document.setLastModifiedDate(LocalDateTime.now());
        return documentRepository.save(document);
    }

    // Obter todos os documentos que um usuário pode acessar
    public List<FormativeDocument> getAccessibleDocuments(User user) {
        // Obter todos os documentos
        List<FormativeDocument> allDocuments = documentRepository.findAll();

        // Filtrar apenas os que o usuário pode acessar
        return allDocuments.stream()
                .filter(user::canAccessDocument)
                .collect(Collectors.toList());
    }

    // Obter documentos específicos de uma etapa
    public List<FormativeDocument> getDocumentsForStage(User.LifeStage stage) {
        return documentRepository.findByDocumentTypeAndAllowedStagesContaining(
                FormativeDocument.DocumentType.STAGE_SPECIFIC, stage);
    }

    // Obter documentos específicos de um local
    public List<FormativeDocument> getDocumentsForLocation(MissionLocation location) {
        return documentRepository.findByDocumentTypeAndAllowedLocationsContaining(
                FormativeDocument.DocumentType.LOCATION_SPECIFIC, location);
    }

    // Registrar progresso de leitura
    public DocumentReadingProgress updateReadingProgress(User user, FormativeDocument document, Integer progressPercentage) throws IllegalAccessException {
        // Verificar se o usuário pode acessar o documento
        if (!user.canAccessDocument(document)) {
            throw new IllegalAccessException("User does not have access to this document");
        }

        // Buscar progresso existente ou criar um novo
        DocumentReadingProgress progress = user.getDocumentProgress(document);
        if (progress == null) {
            progress = new DocumentReadingProgress();
            progress.setUser(user);
            progress.setDocument(document);
            progress.setFirstViewDate(LocalDateTime.now());
        }

        progress.setProgressPercentage(progressPercentage);
        progress.setLastViewDate(LocalDateTime.now());

        if (progressPercentage >= 100) {
            progress.setCompleted(true);
            progress.setCompletedDate(LocalDateTime.now());
        }

        return progressRepository.save(progress);
    }

    // Adicionar permissão para um usuário acessar um documento
    public FormativeDocument grantAccessToUser(FormativeDocument document, User user) {
        if (document.getAllowedUsers() == null) {
            document.setAllowedUsers(new HashSet<>());
        }
        document.getAllowedUsers().add(user);
        return documentRepository.save(document);
    }

    // Adicionar permissão para uma função acessar um documento
    public FormativeDocument grantAccessToRole(FormativeDocument document, Role role) {
        if (document.getAllowedRoles() == null) {
            document.setAllowedRoles(new HashSet<>());
        }
        document.getAllowedRoles().add(role);
        return documentRepository.save(document);
    }

    // Adicionar permissão para uma etapa acessar um documento
    public FormativeDocument grantAccessToStage(FormativeDocument document, User.LifeStage stage) {
        if (document.getAllowedStages() == null) {
            document.setAllowedStages(new HashSet<>());
        }
        document.getAllowedStages().add(stage);
        return documentRepository.save(document);
    }

    // Métodos adicionais para o FormativeDocumentService existente

    // Obter um documento por ID
    public FormativeDocument getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + id));
    }

    // Atualizar um documento
    public FormativeDocument updateDocument(Long id, FormativeDocument documentDetails) {
        FormativeDocument document = getDocumentById(id);

        // Atualizar os campos básicos
        document.setTitle(documentDetails.getTitle());
        document.setContent(documentDetails.getContent());
        document.setDocumentType(documentDetails.getDocumentType());
        document.setAccessLevel(documentDetails.getAccessLevel());
        document.setKeywords(documentDetails.getKeywords());

        // Atualizar anexo se houver
        if (documentDetails.getAttachmentData() != null && documentDetails.getAttachmentData().length > 0) {
            document.setAttachmentData(documentDetails.getAttachmentData());
            document.setAttachmentName(documentDetails.getAttachmentName());
            document.setAttachmentType(documentDetails.getAttachmentType());
        }

        // Atualizar listas de permissões
        if (documentDetails.getAllowedUsers() != null) {
            document.setAllowedUsers(documentDetails.getAllowedUsers());
        }
        if (documentDetails.getAllowedRoles() != null) {
            document.setAllowedRoles(documentDetails.getAllowedRoles());
        }
        if (documentDetails.getAllowedStages() != null) {
            document.setAllowedStages(documentDetails.getAllowedStages());
        }
        if (documentDetails.getAllowedLocations() != null) {
            document.setAllowedLocations(documentDetails.getAllowedLocations());
        }

        return documentRepository.save(document);
    }

    // Excluir um documento
    @Transactional
    public void deleteDocument(Long id) {
        FormativeDocument document = getDocumentById(id);
        documentRepository.delete(document);
    }

    // Adicionar permissão para um local acessar um documento
    public FormativeDocument grantAccessToLocation(FormativeDocument document, MissionLocation location) {
        if (document.getAllowedLocations() == null) {
            document.setAllowedLocations(new HashSet<>());
        }
        document.getAllowedLocations().add(location);
        return documentRepository.save(document);
    }

    // Obter documentos públicos
    public List<FormativeDocument> getPublicDocuments() {
        return documentRepository.findByAccessLevel(FormativeDocument.AccessLevel.PUBLIC);
    }

    // Obter documentos por tipo
    public List<FormativeDocument> getDocumentsByType(FormativeDocument.DocumentType documentType) {
        return documentRepository.findByDocumentType(documentType);
    }
}