package service;

import model.DocumentReadingProgress;
import model.FormativeDocument;
import model.User;
import repository.DocumentReadingProgressRepository;
import repository.FormativeDocumentRepository;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentReadingProgressService {

    @Autowired
    private DocumentReadingProgressRepository progressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FormativeDocumentRepository documentRepository;

    // Obter o progresso de leitura para um usuário e documento específicos
    public DocumentReadingProgress getReadingProgress(Long userId, Long documentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        FormativeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        Optional<DocumentReadingProgress> progress = progressRepository.findByUserAndDocument(user, document);

        return progress.orElse(null);
    }

    // Atualizar o progresso de leitura
    @Transactional
    public DocumentReadingProgress updateReadingProgress(Long userId, Long documentId, Integer progressPercentage, String userNotes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        FormativeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        // Verificar se o usuário pode acessar o documento
        if (!user.canAccessDocument(document)) {
            throw new SecurityException("User does not have access to this document");
        }

        // Buscar progresso existente ou criar um novo
        Optional<DocumentReadingProgress> existingProgress = progressRepository.findByUserAndDocument(user, document);
        DocumentReadingProgress progress;

        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
        } else {
            progress = new DocumentReadingProgress();
            progress.setUser(user);
            progress.setDocument(document);
            progress.setFirstViewDate(LocalDateTime.now());
        }

        // Atualizar o progresso
        progress.setProgressPercentage(progressPercentage);
        progress.setLastViewDate(LocalDateTime.now());

        // Atualizar notas se fornecidas
        if (userNotes != null) {
            progress.setUserNotes(userNotes);
        }

        // Verificar se o documento foi concluído
        if (progressPercentage >= 100 && !progress.getCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedDate(LocalDateTime.now());
        }

        return progressRepository.save(progress);
    }

    // Obter todos os documentos concluídos por um usuário
    public List<DocumentReadingProgress> getCompletedDocumentsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return progressRepository.findByUserAndCompletedTrue(user);
    }

    // Obter documentos em andamento (não concluídos) por um usuário
    public List<DocumentReadingProgress> getInProgressDocumentsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return progressRepository.findByUserAndCompletedFalse(user);
    }

    // Obter o progresso médio para um documento
    public Double getAverageProgressForDocument(Long documentId) {
        FormativeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        return progressRepository.getAverageProgressForDocument(document);
    }

    // Obter documentos recentemente acessados por um usuário
    public List<FormativeDocument> getRecentlyViewedDocuments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return progressRepository.findRecentlyViewedDocuments(user);
    }

    // Limpar o progresso de leitura de um documento para um usuário
    @Transactional
    public void resetReadingProgress(Long userId, Long documentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        FormativeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        Optional<DocumentReadingProgress> progress = progressRepository.findByUserAndDocument(user, document);

        progress.ifPresent(progressRepository::delete);
    }
}