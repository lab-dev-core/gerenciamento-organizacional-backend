package com.gestaoformativa.repository;

import com.gestaoformativa.model.DocumentCategory;
import com.gestaoformativa.model.FormativeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, Long> {

    DocumentCategory findByName(String name);

    List<DocumentCategory> findByParentCategoryIsNull();

    List<DocumentCategory> findByParentCategory(DocumentCategory parentCategory);

    @Query("SELECT c FROM DocumentCategory c JOIN c.documents d WHERE d = :document")
    List<DocumentCategory> findCategoriesForDocument(FormativeDocument document);

    @Query("SELECT COUNT(d) > 0 FROM DocumentCategory c JOIN c.documents d WHERE c = :category")
    boolean hasDocuments(DocumentCategory category);

    List<DocumentCategory> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nameKeyword, String descriptionKeyword);

    @Query("SELECT COUNT(c) > 0 FROM DocumentCategory c WHERE c.parentCategory = :category")
    boolean hasSubcategories(DocumentCategory category);

    long countByParentCategoryIsNull();
}