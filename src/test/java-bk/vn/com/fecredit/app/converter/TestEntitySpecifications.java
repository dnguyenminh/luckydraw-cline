package vn.com.fecredit.app.converter;

import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

/**
 * JPA Specifications for filtering TestEntity objects
 */
public class TestEntitySpecifications {

    /**
     * Filter by entity status
     */
    public static Specification<TestEntity> hasStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }

    /**
     * Filter by multiple entity statuses
     */
    public static Specification<TestEntity> hasStatusIn(Collection<EntityStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return null;
            }
            return root.get("status").in(statuses);
        };
    }

    /**
     * Filter by pattern matching on name
     */
    public static Specification<TestEntity> hasNameLike(String namePattern) {
        return (root, query, cb) -> {
            if (namePattern == null || namePattern.trim().isEmpty()) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), 
                         "%" + namePattern.toLowerCase() + "%");
        };
    }

    /**
     * Filter for active entities only
     */
    public static Specification<TestEntity> isActive() {
        return hasStatus(EntityStatus.ACTIVE).and(
            (root, query, cb) -> cb.isTrue(root.get("active"))
        );
    }

    /**
     * Filter for non-deleted entities
     */
    public static Specification<TestEntity> isNotDeleted() {
        return (root, query, cb) -> 
            cb.notEqual(root.get("status"), EntityStatus.DELETED);
    }

    /**
     * Filter for active entities with name pattern
     */
    public static Specification<TestEntity> activeWithNameLike(String namePattern) {
        return isActive().and(hasNameLike(namePattern));
    }

    /**
     * Filter for deleted or inactive entities
     */
    public static Specification<TestEntity> isDeletedOrInactive() {
        return hasStatus(EntityStatus.DELETED).or(
            hasStatus(EntityStatus.INACTIVE).or(
                (root, query, cb) -> cb.isFalse(root.get("active"))
            )
        );
    }

    /**
     * Combines multiple specifications dynamically
     */
    public static Specification<TestEntity> withFilters(
            EntityStatus status,
            String namePattern,
            Boolean activeOnly
    ) {
        Specification<TestEntity> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and(hasStatus(status));
        }

        if (namePattern != null && !namePattern.trim().isEmpty()) {
            spec = spec.and(hasNameLike(namePattern));
        }

        if (Boolean.TRUE.equals(activeOnly)) {
            spec = spec.and(isActive());
        }

        return spec;
    }
}
