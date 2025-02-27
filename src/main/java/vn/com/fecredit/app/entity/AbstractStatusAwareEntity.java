package vn.com.fecredit.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.converter.StatusConverter;
import vn.com.fecredit.app.enums.EntityStatus;

/**
 * Base class for entities that need status management
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class AbstractStatusAwareEntity extends BaseEntity implements StatusAware {

    @Column(name = "status")
    @Convert(converter = StatusConverter.class)
    private EntityStatus status;

    /**
     * Pre-persist hook to set default status
     */
    protected void prePersist() {
        if (status == null) {
            status = EntityStatus.ACTIVE;
        }
        super.prePersist();
    }

    @Override
    public EntityStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(EntityStatus status) {
        this.status = status;
    }

    /**
     * Set entity as active
     */
    public void setActive(Boolean active) {
        this.status = active ? EntityStatus.ACTIVE : EntityStatus.INACTIVE;
    }

    /**
     * Get if entity is active
     */
    public boolean isActive() {
        return EntityStatus.ACTIVE.equals(status);
    }

    /**
     * Builder specification for status aware entities
     */
    public abstract static class AbstractStatusAwareEntityBuilder<C extends AbstractStatusAwareEntity, B extends AbstractStatusAwareEntityBuilder<C, B>> 
        extends BaseEntity.BaseEntityBuilder<C, B> {
        
        /**
         * Set the active state in builder
         */
        public B active(boolean active) {
            this.status(active ? EntityStatus.ACTIVE : EntityStatus.INACTIVE);
            return self();
        }
    }
}
