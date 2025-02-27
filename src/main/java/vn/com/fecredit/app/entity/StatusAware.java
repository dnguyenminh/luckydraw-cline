package vn.com.fecredit.app.entity;

import vn.com.fecredit.app.enums.EntityStatus;

/**
 * Interface for entities that need to track their status
 */
public interface StatusAware {

    /**
     * Sets the entity status
     */
    void setStatus(EntityStatus status);

    /**
     * Gets the current entity status
     */
    EntityStatus getStatus();
    
    /**
     * Sets the active state
     */
    void setActive(boolean active);

    /**
     * Gets the active state
     */
    Boolean getActive();

    /**
     * Checks if entity is active
     */
    boolean isActive();

    /**
     * Activates the entity
     */
    void activate();

    /**
     * Deactivates the entity
     */
    void deactivate();

}
