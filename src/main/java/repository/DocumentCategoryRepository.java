package repository;

import model.DocumentCategory;
import model.FormativeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, Long> {

    // Buscar por nome
    DocumentCategory findByName(String name);

    // Buscar todas as categorias de nível superior (sem categoria pai)
    List<DocumentCategory> findByParentCategoryIsNull();

    // Buscar subcategorias de uma categoria específica
    List<DocumentCategory> findByParentCategory(DocumentCategory parentCategory);

    // Buscar por documento contido na categoria
    @Query("SELECT c FROM DocumentCategory c JOIN c.documents d WHERE d = :document")
    List<DocumentCategory> findCategoriesForDocument(FormativeDocument document);

    // Verificar se uma categoria tem documentos
    @Query("SELECT COUNT(d) > 0 FROM DocumentCategory c JOIN c.documents d WHERE c = :category")
    boolean hasDocuments(DocumentCategory category);

    // Buscar categorias que contenham uma determinada palavra no nome ou descrição
    List<DocumentCategory> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nameKeyword, String descriptionKeyword);

    // Verificar se uma categoria tem subcategorias
    @Query("SELECT COUNT(c) > 0 FROM DocumentCategory c WHERE c.parentCategory = :category")
    boolean hasSubcategories(DocumentCategory category);

    // Contar categorias de nível superior
    long countByParentCategoryIsNull();
}