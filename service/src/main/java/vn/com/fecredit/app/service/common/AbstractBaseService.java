package vn.com.fecredit.app.service.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.common.PageRequest;
import vn.com.fecredit.app.dto.common.SearchRequest;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.exception.BusinessException;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.util.LoggingUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractBaseService<T extends AbstractStatusAwareEntity, ID, R, C, U>
        implements BaseService<T, ID, R, C, U> {

    protected abstract JpaRepository<T, ID> getRepository();

    protected abstract R mapToResponse(T entity);

    protected abstract List<R> mapToResponseList(List<T> entities);

    protected abstract T mapCreateRequestToEntity(C request);

    protected abstract void mapUpdateRequestToEntity(U request, T entity);

    @Override
    @Transactional
    public R create(C request) {
        LoggingUtils.logMethodEntry("create", request);
        validateCreateRequest(request);

        T entity = mapCreateRequestToEntity(request);
        validateBeforeSave(entity);

        entity = getRepository().save(entity);
        R response = mapToResponse(entity);

        LoggingUtils.logMethodExit("create", response);
        return response;
    }

    @Override
    @Transactional
    public R update(ID id, U request) {
        LoggingUtils.logMethodEntry("update", id, request);
        validateUpdateRequest(id, request);

        T entity = findEntityById(id);
        if (!hasPermission(entity)) {
            throw new BusinessException("No permission to update this entity");
        }

        mapUpdateRequestToEntity(request, entity);
        validateBeforeSave(entity);

        entity = getRepository().save(entity);
        R response = mapToResponse(entity);

        LoggingUtils.logMethodExit("update", response);
        return response;
    }

    @Override
    public R getById(ID id) {
        return findById(id).orElseThrow(() ->
                new ResourceNotFoundException(getEntityClass().getSimpleName(), "id", id));
    }

    @Override
    public Optional<R> findById(ID id) {
        return getRepository().findById(id)
                .filter(this::hasPermission)
                .map(this::mapToResponse);
    }

    @Override
    public List<R> getAll() {
        List<T> entities = getRepository().findAll().stream()
                .filter(this::hasPermission)
                .collect(Collectors.toList());
        return mapToResponseList(entities);
    }

    @Override
    public List<R> getActive() {
        List<T> entities = getRepository().findAll().stream()
                .filter(entity -> Boolean.TRUE.equals(entity.isActive()))
                .filter(this::hasPermission)
                .collect(Collectors.toList());
        return mapToResponseList(entities);
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        T entity = findEntityById(id);
        if (!hasPermission(entity)) {
            throw new BusinessException("No permission to delete this entity");
        }

        validateBeforeDelete(entity);
        getRepository().delete(entity);
    }

    @Override
    public Page<R> getPage(PageRequest pageRequest) {
        return getRepository()
                .findAll(pageRequest.toSpringPageRequest())
                .map(this::mapToResponse);
    }

    @Override
    public Page<R> search(SearchRequest searchRequest) {
        validateSearchRequest(searchRequest);
        return doSearch(searchRequest);
    }

    protected Page<R> doSearch(SearchRequest searchRequest) {
        // Default implementation - override in concrete services
        return getPage(searchRequest);
    }

    @Override
    public boolean existsById(ID id) {
        return getRepository().existsById(id);
    }

    @Override
    @Transactional
    public R activate(ID id) {
        T entity = findEntityById(id);
        if (!hasPermission(entity)) {
            throw new BusinessException("No permission to activate this entity");
        }

        if (Boolean.TRUE.equals(entity.isActive())) {
            throw new BusinessException("Entity is already active");
        }

        entity.setStatus(AbstractStatusAwareEntity.STATUS_ACTIVE);
        entity = getRepository().save(entity);
        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public R deactivate(ID id) {
        T entity = findEntityById(id);
        if (!hasPermission(entity)) {
            throw new BusinessException("No permission to deactivate this entity");
        }

        if (Boolean.FALSE.equals(entity.isActive())) {
            throw new BusinessException("Entity is already inactive");
        }

        entity.setStatus(AbstractStatusAwareEntity.STATUS_INACTIVE);
        entity = getRepository().save(entity);
        return mapToResponse(entity);
    }

    @Override
    public long count() {
        return getRepository().count();
    }

    @Override
    public long countActive() {
        return getRepository().findAll().stream()
                .filter(entity -> Boolean.TRUE.equals(entity.isActive()))
                .count();
    }

    protected T findEntityById(ID id) {
        return getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getEntityClass().getSimpleName(), "id", id));
    }

    protected void validateEntity(T entity, String operation) {
        if (entity == null) {
            throw new BusinessException("Entity cannot be null for " + operation);
        }
    }

    protected void validateId(ID id, String operation) {
        if (id == null) {
            throw new BusinessException("ID cannot be null for " + operation);
        }
    }
}
