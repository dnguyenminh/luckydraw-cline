package vn.com.fecredit.app.specifications;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;

import jakarta.persistence.criteria.Join;

public class EventSpecifications {

    public static Specification<Event> searchEvents(String name, String location, String status) {
        return Specification
            .where(hasName(name))
            .and(hasLocation(location))
            .and(hasStatus(status));
    }

    private static Specification<Event> hasName(String name) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(name)) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    private static Specification<Event> hasLocation(String location) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(location)) {
                return null;
            }
            Join<Event, EventLocation> locationJoin = root.join("locations");
            return cb.like(cb.lower(locationJoin.get("name")), "%" + location.toLowerCase() + "%");
        };
    }

    private static Specification<Event> hasStatus(String status) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(status)) {
                return null;
            }
            try {
                EntityStatus entityStatus = EntityStatus.valueOf(status.toUpperCase());
                return cb.equal(root.get("status"), entityStatus);
            } catch (IllegalArgumentException e) {
                return null;
            }
        };
    }
}
