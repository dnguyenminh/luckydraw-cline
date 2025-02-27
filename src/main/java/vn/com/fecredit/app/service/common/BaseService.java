package vn.com.fecredit.app.service.common;

import org.springframework.data.domain.Page;
import vn.com.fecredit.app.dto.common.PageRequest;
import vn.com.fecredit.app.dto.common.SearchRequest;
import vn.com.fecredit.app.entity.BaseEntity;

import java.util.List;
import java.util.Optional;

/**
 * Base service interface providing common CRUD and search operations
 * @param <T> Entity type
 * @param <ID> ID type
 * @param <R> Response DTO type
 * @param <C> Create request type
 * @param <U> Update request type
 */
public interface BaseService<T extends BaseEntity, ID, R, C, U> {

    /**
     * Create new entity
     */
    R create(C request);

    /**
     * Update existing entity
     */
    R update(ID id, U request);

    /**
     * Get entity by ID
     */
    R getById(ID id);

    /**
     * Get entity by ID as Optional
     */
    Optional<R> findById(ID id);

    /**
     * Get all entities
     */
    List<R> getAll();

    /**
     * Get active entities
     */
    List<R> getActive();

    /**
     * Delete entity by ID
     */
    void deleteById(ID id);

    /**
     * Get paginated results
     */
    Page<R> getPage(PageRequest pageRequest);

    /**
     * Search with criteria
     */
    Page<R> search(SearchRequest searchRequest);

    /**
     * Check if entity exists
     */
    boolean existsById(ID id);

    /**
     * Activate entity
     */
    R activate(ID id);

    /**
     * Deactivate entity
     */
    R deactivate(ID id);

    /**
     * Count total entities
     */
    long count();

    /**
     * Count active entities
     */
    long countActive();

    /**
     * Get underlying entity class
     */
    Class<T> getEntityClass();

    /**
     * Get response DTO class
     */
    Class<R> getResponseClass();

    /**
     * Validate entity before save
     */
    default void validateBeforeSave(T entity) {
        // Default implementation - override in concrete services
    }

    /**
     * Validate entity before delete
     */
    default void validateBeforeDelete(T entity) {
        // Default implementation - override in concrete services
    }

    /**
     * Custom validation for create request
     */
    default void validateCreateRequest(C request) {
        // Default implementation - override in concrete services
    }

    /**
     * Custom validation for update request
     */
    default void validateUpdateRequest(ID id, U request) {
        // Default implementation - override in concrete services
    }

    /**
     * Custom validation for search request
     */
    default void validateSearchRequest(SearchRequest request) {
        // Default implementation - override in concrete services
    }

    /**
     * Check if user has permission to access entity
     */
    default boolean hasPermission(T entity) {
        return true; // Default implementation - override in concrete services
    }
}
