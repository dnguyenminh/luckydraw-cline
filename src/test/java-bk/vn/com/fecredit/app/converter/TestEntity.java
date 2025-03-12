package vn.com.fecredit.app.converter;

import jakarta.persistence.*;
import lombok.*;

/**
 * Test entity for status converter tests
 */
@Entity
@Table(name = "test_entities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = EntityStatusConverter.class)
    @Column(name = "status")
    private EntityStatus status;

    @Column(name = "name")
    private String name;

    @Column(name = "active")
    private boolean active = true;

    public TestEntity(String name) {
        this.name = name;
        this.status = EntityStatus.DRAFT;
        this.active = true;
    }

    public TestEntity(String name, EntityStatus status) {
        this.name = name;
        this.status = status;
        this.active = true;
    }

    public boolean isActive() {
        return active && (status == EntityStatus.ACTIVE);
    }

    public boolean isDeleted() {
        return status == EntityStatus.DELETED;
    }

    public boolean isInactive() {
        return !active || status.isInactive();
    }

    public void markAsDeleted() {
        this.status = EntityStatus.DELETED;
        this.active = false;
    }

    public void makeInactive() {
        this.status = EntityStatus.INACTIVE;
        this.active = false;
    }
}
