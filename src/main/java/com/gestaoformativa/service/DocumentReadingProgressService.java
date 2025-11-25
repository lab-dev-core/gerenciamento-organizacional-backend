package com.gestaoformativa.service;

import com.gestaoformativa.model.DocumentReadingProgress;
import com.gestaoformativa.model.FormativeDocument;
import com.gestaoformativa.model.User;
import com.gestaoformativa.repository.DocumentReadingProgressRepository;
import com.gestaoformativa.repository.FormativeDocumentRepository;
import com.gestaoformativa.repository.UserRepository;
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

    public DocumentReadingProgress getReadingProgress(Long userId, Long documentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        FormativeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        Optional<DocumentReadingProgress> progress = progressRepository.findByUserAndDocument(user, document);

        return progress.orElse(null);
    }

    @Transactional
    public DocumentReadingProgress updateReadingProgress(Long userId, Long documentId, Integer progressPercentage, String userNotes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        FormativeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        if (!user.canAccessDocument(document)) {
            throw new SecurityException("User does not have access to this document");
        }

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

        progress.setProgressPercentage(progressPercentage);
        progress.setLastViewDate(LocalDateTime.now());

        if (userNotes != null) {
            progress.setUserNotes(userNotes);
        }

        if (progressPercentage >= 100 && !progress.getCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedDate(LocalDateTime.now());
        }

        return progressRepository.save(progress);
    }

    public List<DocumentReadingProgress> getCompletedDocumentsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return progressRepository.findByUserAndCompletedTrue(user);
    }

    public List<DocumentReadingProgress> getInProgressDocumentsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return progressRepository.findByUserAndCompletedFalse(user);
    }

    public Double getAverageProgressForDocument(Long documentId) {
        FormativeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        return progressRepository.getAverageProgressForDocument(document);
    }

    public List<FormativeDocument> getRecentlyViewedDocuments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return progressRepository.findRecentlyViewedDocuments(user);
    }

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