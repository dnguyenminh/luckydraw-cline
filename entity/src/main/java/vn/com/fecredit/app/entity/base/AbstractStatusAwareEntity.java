package vn.com.fecredit.app.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractStatusAwareEntity extends AbstractAuditEntity {

    private static final long serialVersionUID = 1L;

    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_DELETED = -1;

    @Column(name = "status", nullable = false)
    @Builder.Default
    protected int status = STATUS_ACTIVE;

    public boolean isActive() {
        return status == STATUS_ACTIVE;
    }

    public void activate() {
        onActivate();
        this.status = STATUS_ACTIVE;
    }

    public void deactivate() {
        onDeactivate();
        this.status = STATUS_INACTIVE;
    }

    public String getStatusName() {
        return status == STATUS_ACTIVE ? "Active" : "Inactive";
    }

    /**
     * Called before activation. Override to add custom activation logic.
     */
    protected void onActivate() {
        // Default implementation does nothing
    }

    /**
     * Called before deactivation. Override to add custom deactivation logic.
     */
    protected void onDeactivate() {
        // Default implementation does nothing
    }
}
