package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import vn.com.fecredit.app.entity.converter.StatusConverter;
import vn.com.fecredit.app.enums.EntityStatus;

import java.time.LocalDateTime;

/**
 * Base entity class that provides common fields and functionality
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Versionable, StatusAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Long version;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "status")
    @Convert(converter = StatusConverter.class)
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "active")
    private boolean active = true;

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastModifiedAt == null) {
            lastModifiedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = EntityStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        lastModifiedAt = LocalDateTime.now();
    }

    @Override
    public Long getVersion() {
        return version;
    }

    @Override 
    public void setStatus(EntityStatus status) {
        this.status = status;
        this.active = (status == EntityStatus.ACTIVE);
    }

    @Override
    public EntityStatus getStatus() {
        return this.status;
    }

    @Override
    public void activate() {
        setStatus(EntityStatus.ACTIVE);
    }

    @Override
    public void deactivate() {
        setStatus(EntityStatus.INACTIVE);
    }

    @Override
    public boolean isActive() {
        return active && !deleted;
    }

    @Override 
    public void setActive(boolean active) {
        this.active = active;
        this.status = active ? EntityStatus.ACTIVE : EntityStatus.INACTIVE;
    }

    @Override
    public Boolean getActive() {
        return active;
    }
}
