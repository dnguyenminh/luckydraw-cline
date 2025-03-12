package vn.com.fecredit.app.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.converter.EntityStatusConverterIntegrationTest.TestEntity;

import java.util.List;
import java.util.Optional;

import static vn.com.fecredit.app.converter.TestEntitySpecifications.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestEntityService {

    private final TestEntityRepository repository;

    @Transactional
    public TestEntity create(String name) {
        TestEntity entity = new TestEntity();
        entity.setName(name);
        entity.setStatus(EntityStatus.ACTIVE);
        return repository.save(entity);
    }

    @Transactional
    public Optional<TestEntity> updateStatus(Long id, EntityStatus newStatus) {
        return repository.findById(id)
            .map(entity -> {
                entity.setStatus(newStatus);
                return repository.save(entity);
            });
    }

    @Transactional
    public void softDelete(Long id) {
        repository.findById(id)
            .ifPresent(entity -> entity.setStatus(EntityStatus.DELETED));
    }

    public List<TestEntity> findActive() {
        return repository.findAll(isActive());
    }

    public List<TestEntity> findActiveByNamePattern(String namePattern) {
        return repository.findAll(
            Specification.where(isActive())
                .and(hasNameLike(namePattern))
        );
    }

    public List<TestEntity> findNonDeleted() {
        return repository.findAll(isNotDeleted());
    }

    public Page<TestEntity> findAllActive(Pageable pageable) {
        return repository.findAll(isActive(), pageable);
    }

    public List<TestEntity> findByStatusAndNamePattern(EntityStatus status, String namePattern) {
        return repository.findAll(
            Specification.where(hasStatus(status))
                .and(hasNameLike(namePattern))
        );
    }

    public long countByStatus(EntityStatus status) {
        return repository.count(hasStatus(status));
    }

    @Transactional
    public void deactivateStale() {
        repository.findAll(isActive()).forEach(entity -> 
            entity.setStatus(EntityStatus.INACTIVE)
        );
    }

    @Transactional
    public void archive(Long id) {
        repository.findById(id)
            .filter(entity -> entity.getStatus() != EntityStatus.DELETED)
            .ifPresent(entity -> entity.setStatus(EntityStatus.ARCHIVED));
    }

    public boolean hasEntitiesInStatus(EntityStatus status) {
        return repository.exists(hasStatus(status));
    }

    public List<TestEntity> findDeletedOrInactive() {
        return repository.findAll(isDeletedOrInactive());
    }
}
