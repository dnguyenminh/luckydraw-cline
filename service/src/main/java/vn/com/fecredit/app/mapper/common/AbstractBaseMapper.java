package vn.com.fecredit.app.mapper.common;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract base mapper for converting between entities and DTOs
 * @param <E> Entity type
 * @param <D> DTO type
 */
public abstract class AbstractBaseMapper<E, D> {
    
    /**
     * Convert entity to DTO
     */
    public abstract D toDto(E entity);

    /**
     * Convert DTO to entity
     */
    public abstract E toEntity(D dto);

    /**
     * Convert collection of entities to list of DTOs
     */
    public List<D> toDtoList(Collection<E> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert collection of DTOs to list of entities
     */
    public List<E> toEntityList(Collection<D> dtos) {
        if (dtos == null) {
            return Collections.emptyList();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
