package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;

/**
 * Interface for entities that need versioning and auditing
 */
public interface Versionable {

    /**
     * Get version number
     */
    Long getVersion();

    /**
     * Set version number
     */
    void setVersion(Long version);

    /**
     * Get entity creation time
     */
    LocalDateTime getCreatedAt();

    /**
     * Set entity creation time
     */
    void setCreatedAt(LocalDateTime createdAt);

    /**
     * Get user who created the entity
     */
    String getCreatedBy();

    /**
     * Set user who created the entity 
     */
    void setCreatedBy(String createdBy);

    /**
     * Get last modification time
     */
    LocalDateTime getLastModifiedAt();

    /**
     * Set last modification time
     */
    void setLastModifiedAt(LocalDateTime lastModifiedAt);

    /**
     * Get user who last modified the entity
     */
    String getLastModifiedBy();

    /**
     * Set user who last modified the entity
     */
    void setLastModifiedBy(String lastModifiedBy);

    /**
     * Get deletion time
     */
    LocalDateTime getDeletedAt();

    /**
     * Set deletion time
     */
    void setDeletedAt(LocalDateTime deletedAt);

    /**
     * Get user who deleted the entity
     */
    String getDeletedBy();

    /**
     * Set user who deleted the entity
     */
    void setDeletedBy(String deletedBy);

    /**
     * Check if entity is deleted
     */
    boolean isDeleted();

    /**
     * Set deleted status
     */
    void setDeleted(boolean deleted);
}
