package service;

import model.DocumentCategory;
import model.FormativeDocument;
import repository.DocumentCategoryRepository;
import repository.FormativeDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentCategoryService {

    @Autowired
    private DocumentCategoryRepository categoryRepository;

    @Autowired
    private FormativeDocumentRepository documentRepository;

    @Transactional
    public DocumentCategory removeDocumentFromCategory(Long categoryId, Long documentId) {
        DocumentCategory category = getCategoryById(categoryId);
        FormativeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado com id: " + documentId));

        if (category.getDocuments() != null) {
            category.getDocuments().removeIf(doc -> doc.getId().equals(documentId));
            return categoryRepository.save(category);
        } else {
            throw new IllegalStateException("A categoria não contém o documento especificado");
        }
    }

    // Método para adicionar um documento a uma categoria (complemento)
    @Transactional
    public DocumentCategory addDocumentToCategory(Long categoryId, Long documentId) {
        DocumentCategory category = getCategoryById(categoryId);
        FormativeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado com id: " + documentId));

        if (category.getDocuments() == null) {
            category.setDocuments(new ArrayList<>());
        }

        if (!category.getDocuments().contains(document)) {
            category.getDocuments().add(document);
        }

        return categoryRepository.save(category);
    }

    // Método para obter todas as categorias
    public List<DocumentCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Método para obter categorias de nível superior (raiz)
    public List<DocumentCategory> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull();
    }

    // Método para obter subcategorias de uma categoria
    public List<DocumentCategory> getSubcategories(Long categoryId) {
        DocumentCategory parentCategory = getCategoryById(categoryId);
        return categoryRepository.findByParentCategory(parentCategory);
    }

    // Método para obter categoria por ID
    public DocumentCategory getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com id: " + id));
    }

    // Método para criar uma nova categoria
    public DocumentCategory createCategory(DocumentCategory category) {
        return categoryRepository.save(category);
    }

    // Método para atualizar uma categoria
    @Transactional
    public DocumentCategory updateCategory(Long id, DocumentCategory categoryDetails) {
        DocumentCategory category = getCategoryById(id);

        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setParentCategory(categoryDetails.getParentCategory());

        return categoryRepository.save(category);
    }

    // Método para excluir uma categoria
    @Transactional
    public void deleteCategory(Long id) {
        DocumentCategory category = getCategoryById(id);

        // Verificar se a categoria tem subcategorias
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir uma categoria que possui subcategorias");
        }

        categoryRepository.delete(category);
    }
}