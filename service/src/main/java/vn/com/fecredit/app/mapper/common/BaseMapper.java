package vn.com.fecredit.app.mapper.common;

import org.springframework.data.domain.Page;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base mapper interface for converting between entities and DTOs
 * @param <E> Entity type
 * @param <R> Response DTO type
 * @param <C> Create request type
 * @param <U> Update request type
 */
public interface BaseMapper<E extends AbstractStatusAwareEntity, R, C, U> {

    /**
     * Convert entity to response DTO
     */
    R toResponse(E entity);

    /**
     * Convert create request to entity
     */
    E toEntity(C createRequest);

    /**
     * Update entity from update request
     */
    void updateEntity(U updateRequest, E entity);

    /**
     * Convert collection of entities to list of response DTOs
     */
    default List<R> toResponseList(Collection<E> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert collection of entities to set of response DTOs
     */
    default Set<R> toResponseSet(Collection<E> entities) {
        if (entities == null) {
            return Set.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }

    /**
     * Convert page of entities to page of response DTOs
     */
    default Page<R> toResponsePage(Page<E> page) {
        if (page == null) {
            return Page.empty();
        }
        return page.map(this::toResponse);
    }

    /**
     * Convert collection of create requests to list of entities
     */
    default List<E> toEntityList(Collection<C> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Convert collection of create requests to set of entities
     */
    default Set<E> toEntitySet(Collection<C> requests) {
        if (requests == null) {
            return Set.of();
        }
        return requests.stream()
                .map(this::toEntity)
                .collect(Collectors.toSet());
    }

    /**
     * Update collection of entities from update requests
     */
    default void updateEntities(Collection<U> requests, Collection<E> entities) {
        if (requests == null || entities == null || requests.size() != entities.size()) {
            throw new IllegalArgumentException("Collections must not be null and must have same size");
        }
        var iterator1 = requests.iterator();
        var iterator2 = entities.iterator();
        while (iterator1.hasNext() && iterator2.hasNext()) {
            updateEntity(iterator1.next(), iterator2.next());
        }
    }

    /**
     * Get entity class
     */
    Class<E> getEntityClass();

    /**
     * Get response DTO class
     */
    Class<R> getResponseClass();

    /**
     * Get create request class
     */
    Class<C> getCreateRequestClass();

    /**
     * Get update request class
     */
    Class<U> getUpdateRequestClass();
}
