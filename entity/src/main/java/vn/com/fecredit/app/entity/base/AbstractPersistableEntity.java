package vn.com.fecredit.app.entity.base;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
public abstract class AbstractPersistableEntity implements Persistable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected AbstractPersistableEntity() {
        this.version = 0L;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AbstractPersistableEntity other = (AbstractPersistableEntity) obj;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s[id=%d]", getClass().getSimpleName(), getId());
    }

    public boolean isPersisted() {
        return id != null;
    }

    public boolean isModified() {
        return version > 0L;
    }

    public AbstractPersistableEntity toReference() {
        try {
            AbstractPersistableEntity reference = getClass().getDeclaredConstructor().newInstance();
            reference.setId(this.id);
            return reference;
        } catch (Exception e) {
            throw new RuntimeException("Could not create reference for " + getClass(), e);
        }
    }

    public boolean isReference() {
        return id != null && version == null;
    }
}
