package vn.com.fecredit.app.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Column(name = "status", nullable = false)
    @Builder.Default
    protected int status = STATUS_ACTIVE;

    public boolean isActive() {
        return status == STATUS_ACTIVE;
    }

    public void activate() {
        this.status = STATUS_ACTIVE;
    }

    public void deactivate() {
        this.status = STATUS_INACTIVE;
    }

    public String getStatusName() {
        return status == STATUS_ACTIVE ? "Active" : "Inactive";
    }

    /**
     * Helper method to create a deep copy of collections when using toBuilder()
     */
    @SuppressWarnings("unchecked")
    protected static <T> T deepCopyIfNeeded(T obj) {
        if (obj instanceof List) {
            return (T) new ArrayList<>((List<?>) obj);
        }
        if (obj instanceof Set) {
            return (T) new HashSet<>((Set<?>) obj);
        }
        if (obj instanceof Map) {
            return (T) new HashMap<>((Map<?, ?>) obj);
        }
        return obj;
    }
}
