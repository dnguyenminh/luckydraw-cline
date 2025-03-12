package vn.com.fecredit.app.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.converter.EntityStatusConverterIntegrationTest.TestEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TestEntityServiceTest {

    @Autowired
    private TestEntityService service;

    @BeforeEach
    void setUp() {
        // Create test entities with various statuses
        service.create("Active Entity 1");
        service.create("Active Entity 2");
        service.create("To Be Archived");
        service.create("To Be Deleted");
        service.create("To Be Inactive");
        
        // Update statuses
        service.updateStatus(3L, EntityStatus.ARCHIVED);
        service.updateStatus(4L, EntityStatus.DELETED);
        service.updateStatus(5L, EntityStatus.INACTIVE);
    }

    @Test
    void shouldCreateActiveEntity() {
        // when
        TestEntity entity = service.create("New Entity");
        
        // then
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(entity.getName()).isEqualTo("New Entity");
    }

    @Test
    void shouldUpdateStatus() {
        // when
        service.updateStatus(1L, EntityStatus.ARCHIVED);
        
        // then
        List<TestEntity> archivedEntities = service.findByStatusAndNamePattern(
            EntityStatus.ARCHIVED, "Active Entity 1"
        );
        assertThat(archivedEntities).hasSize(1);
    }

    @Test
    void shouldSoftDelete() {
        // when
        service.softDelete(1L);
        
        // then
        assertThat(service.findActive())
            .extracting("name")
            .doesNotContain("Active Entity 1");
    }

    @Test
    void shouldFindActiveEntities() {
        // when
        List<TestEntity> activeEntities = service.findActive();
        
        // then
        assertThat(activeEntities)
            .hasSize(2)
            .extracting("name")
            .containsExactlyInAnyOrder("Active Entity 1", "Active Entity 2");
    }

    @Test
    void shouldFindActiveByNamePattern() {
        // when
        List<TestEntity> entities = service.findActiveByNamePattern("Entity 1");
        
        // then
        assertThat(entities)
            .hasSize(1)
            .extracting("name")
            .containsExactly("Active Entity 1");
    }

    @Test
    void shouldFindNonDeletedEntities() {
        // when
        List<TestEntity> nonDeleted = service.findNonDeleted();
        
        // then
        assertThat(nonDeleted)
            .hasSize(4)
            .extracting("name")
            .doesNotContain("To Be Deleted");
    }

    @Test
    void shouldPaginateActiveEntities() {
        // when
        Page<TestEntity> page = service.findAllActive(
            PageRequest.of(0, 1, Sort.by("name"))
        );
        
        // then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Active Entity 1");
    }

    @Test
    void shouldDeactivateStaleEntities() {
        // when
        service.deactivateStale();
        
        // then
        assertThat(service.findActive()).isEmpty();
        assertThat(service.countByStatus(EntityStatus.INACTIVE)).isEqualTo(3);
    }

    @Test
    void shouldArchiveEntity() {
        // when
        service.archive(1L);
        
        // then
        assertThat(service.hasEntitiesInStatus(EntityStatus.ARCHIVED)).isTrue();
        assertThat(service.findActive())
            .extracting("name")
            .doesNotContain("Active Entity 1");
    }

    @Test
    void shouldNotArchiveDeletedEntity() {
        // given
        Long deletedEntityId = 4L;
        
        // when
        service.archive(deletedEntityId);
        
        // then
        assertThat(service.findByStatusAndNamePattern(EntityStatus.ARCHIVED, "To Be Deleted"))
            .isEmpty();
    }

    @Test
    void shouldFindDeletedOrInactiveEntities() {
        // when
        List<TestEntity> entities = service.findDeletedOrInactive();
        
        // then
        assertThat(entities)
            .hasSize(2)
            .extracting("name")
            .containsExactlyInAnyOrder("To Be Deleted", "To Be Inactive");
    }
}
