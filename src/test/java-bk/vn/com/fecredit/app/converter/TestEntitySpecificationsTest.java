package vn.com.fecredit.app.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import vn.com.fecredit.app.converter.EntityStatusConverterIntegrationTest.TestEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static vn.com.fecredit.app.converter.TestEntitySpecifications.*;

@DataJpaTest
class TestEntitySpecificationsTest {

    @Autowired
    private TestEntityRepository repository;

    @BeforeEach
    void setUp() {
        // Create test data
        createTestEntity("Active Entity 1", EntityStatus.ACTIVE);
        createTestEntity("Active Entity 2", EntityStatus.ACTIVE);
        createTestEntity("Inactive Entity", EntityStatus.INACTIVE);
        createTestEntity("Archived Entity", EntityStatus.ARCHIVED);
        createTestEntity("Deleted Entity", EntityStatus.DELETED);
        createTestEntity("Draft Entity", EntityStatus.DRAFT);
    }

    @Test
    void shouldFindByStatus() {
        // when
        List<TestEntity> activeEntities = repository.findAll(hasStatus(EntityStatus.ACTIVE));
        
        // then
        assertThat(activeEntities).hasSize(2)
            .extracting("name")
            .containsExactlyInAnyOrder("Active Entity 1", "Active Entity 2");
    }

    @Test
    void shouldFindByMultipleStatuses() {
        // when
        List<TestEntity> entities = repository.findAll(
            hasStatusIn(Arrays.asList(EntityStatus.ACTIVE, EntityStatus.ARCHIVED))
        );
        
        // then
        assertThat(entities).hasSize(3)
            .extracting("name")
            .containsExactlyInAnyOrder("Active Entity 1", "Active Entity 2", "Archived Entity");
    }

    @Test
    void shouldFindActiveEntitiesByNamePattern() {
        // when
        List<TestEntity> entities = repository.findAll(
            activeWithNameLike("Entity 1")
        );
        
        // then
        assertThat(entities).hasSize(1)
            .extracting("name")
            .containsExactly("Active Entity 1");
    }

    @Test
    void shouldFindNonDeletedEntities() {
        // when
        List<TestEntity> entities = repository.findAll(isNotDeleted());
        
        // then
        assertThat(entities).hasSize(5)
            .extracting("name")
            .doesNotContain("Deleted Entity");
    }

    @Test
    void shouldCombineSpecifications() {
        // when
        Specification<TestEntity> spec = Specification
            .where(isNotDeleted())
            .and(hasNameLike("Entity 1"))
            .or(hasNameLike("Entity 2"));
            
        List<TestEntity> entities = repository.findAll(spec);
        
        // then
        assertThat(entities).hasSize(2)
            .extracting("name")
            .containsExactlyInAnyOrder("Active Entity 1", "Active Entity 2");
    }

    @Test
    void shouldWorkWithPaginationAndSorting() {
        // when
        Page<TestEntity> page = repository.findAll(
            isNotDeleted(),
            PageRequest.of(0, 2, Sort.by("name").ascending())
        );
        
        // then
        assertThat(page.getContent()).hasSize(2)
            .extracting("name")
            .containsExactly("Active Entity 1", "Active Entity 2");
        assertThat(page.getTotalElements()).isEqualTo(5);
    }

    @Test
    void shouldHandleNullParameters() {
        // when
        List<TestEntity> entitiesWithNullStatus = repository.findAll(hasStatus(null));
        List<TestEntity> entitiesWithNullName = repository.findAll(hasNameLike(null));
        
        // then
        assertThat(entitiesWithNullStatus).hasSize(6); // Returns all when spec is null
        assertThat(entitiesWithNullName).hasSize(6);
    }

    private TestEntity createTestEntity(String name, EntityStatus status) {
        TestEntity entity = new TestEntity();
        entity.setName(name);
        entity.setStatus(status);
        return repository.save(entity);
    }
}
