package service;

import model.FormativeDocument;
import model.MissionLocation;
import model.User;
import repository.DocumentSearchRepository;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentSearchService {

    @Autowired
    private DocumentSearchRepository searchRepository;

    @Autowired
    private UserRepository userRepository;

    // Busca avançada de documentos com múltiplos critérios
    public Page<FormativeDocument> searchDocuments(
            String title,
            Long authorId,
            FormativeDocument.DocumentType documentType,
            FormativeDocument.AccessLevel accessLevel,
            User.LifeStage stage,
            Long locationId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String keyword,
            Pageable pageable) {

        User author = null;
        if (authorId != null) {
            author = userRepository.findById(authorId)
                    .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + authorId));
        }

        MissionLocation location = null;
        if (locationId != null) {
            // Obter o local do repositório adequado
            // location = locationRepository.findById(locationId).orElse(null);
        }

        return searchRepository.searchDocuments(
                title, author, documentType, accessLevel, stage,
                location, fromDate, toDate, keyword, pageable);
    }

    // Busca por texto no conteúdo dos documentos
    public List<FormativeDocument> searchByContent(String text) {
        return searchRepository.searchByContent(text);
    }

    // Obter documentos recentemente atualizados
    public List<FormativeDocument> getRecentlyUpdatedDocuments() {
        return searchRepository.findTop10ByOrderByLastModifiedDateDesc();
    }

    // Obter documentos mais vistos
    public List<FormativeDocument> getMostViewedDocuments(Pageable pageable) {
        return searchRepository.findMostViewedDocuments(pageable);
    }

    // Obter documentos recomendados para um usuário
    public List<FormativeDocument> getRecommendedDocumentsForUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return searchRepository.findRecommendedDocumentsForUser(user, pageable);
    }
}