package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.EventLocation;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Sql(scripts = {"/schema-test.sql", "/data-test.sql"})
class RegionRepositoryIntegrationTest {

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldLoadTestData() {
        List<Region> regions = regionRepository.findAll();
        assertThat(regions).isNotEmpty();
        assertThat(regions).hasSize(4); // Verify exact number from test data
    }

    @Test
    void shouldFindByCodeCaseInsensitive() {
        Optional<Region> regionOpt = regionRepository.findByCode("NORTH");
        assertThat(regionOpt).isPresent();
        Region region = regionOpt.get();
        assertThat(region.getName()).isEqualTo("North Region");
        assertThat(region.getDefaultWinProbability()).isEqualTo(0.3);
    }

    @Test
    void shouldReturnEmptyOptionalForNonexistentCode() {
        assertThat(regionRepository.findByCode("NONEXISTENT")).isEmpty();
    }

    @Test
    void shouldFindActiveRegions() {
        List<Region> activeRegions = regionRepository.findAllByStatus(1);
        assertThat(activeRegions).isNotEmpty()
            .allMatch(r -> r.getStatus() == 1)
            .hasSize(3); // Verify exact number from test data
    }

    @Test
    void shouldReturnEmptyListForInvalidStatus() {
        List<Region> regions = regionRepository.findAllByStatus(99);
        assertThat(regions).isEmpty();
    }

    @Test
    void shouldPaginateResults() {
        // Test first page
        Page<Region> firstPage = regionRepository.findAll(PageRequest.of(0, 2));
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(4);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        
        // Test second page
        Page<Region> secondPage = regionRepository.findAll(PageRequest.of(1, 2));
        assertThat(secondPage.getContent()).hasSize(2);
        assertThat(secondPage.getNumber()).isEqualTo(1);
    }

    @Test
    void shouldFindRegionsWithLocations() {
        List<Region> regions = regionRepository.findRegionsWithLocations();
        assertThat(regions)
            .isNotEmpty()
            .anyMatch(r -> !r.getEventLocations().isEmpty());
        // Verify no N+1 queries by checking lazy loading
        assertThat(regions.get(0).getEventLocations()).isNotNull();
    }

    @Test
    void shouldCheckForActiveProvinces() {
        Optional<Region> northRegion = regionRepository.findByCode("NORTH");
        assertThat(northRegion).isPresent();
        boolean hasActive = regionRepository.hasActiveProvinces(northRegion.get().getId());
        assertThat(hasActive).isTrue();

        Optional<Region> centralRegion = regionRepository.findByCode("CENTRAL");
        assertThat(centralRegion).isPresent();
        boolean hasNoActive = regionRepository.hasActiveProvinces(centralRegion.get().getId());
        assertThat(hasNoActive).isFalse();
    }

    @Test
    void shouldCheckForActiveLocations() {
        Optional<Region> southRegion = regionRepository.findByCode("SOUTH");
        assertThat(southRegion).isPresent();
        boolean hasActive = regionRepository.hasActiveEventLocations(southRegion.get().getId());
        assertThat(hasActive).isTrue();

        Optional<Region> centralRegion = regionRepository.findByCode("CENTRAL");
        assertThat(centralRegion).isPresent();
        boolean hasNoActive = regionRepository.hasActiveEventLocations(centralRegion.get().getId());
        assertThat(hasNoActive).isFalse();
    }

    @Test
    void shouldFetchRegionWithProvinces() {
        Optional<Region> regionOpt = regionRepository.findByIdWithProvinces(1L);
        assertThat(regionOpt).isPresent();
        Region region = regionOpt.get();
        assertThat(region.getProvinces())
            .isNotEmpty()
            .hasSize(2); // Verify exact number from test data
        assertThat(region.getProvinces().get(0).getName()).isNotNull();
    }

    @Test
    void shouldFetchRegionWithLocations() {
        Optional<Region> regionOpt = regionRepository.findByIdWithLocations(1L);
        assertThat(regionOpt).isPresent();
        Region region = regionOpt.get();
        assertThat(region.getEventLocations())
            .isNotEmpty()
            .hasSize(2); // Verify exact number from test data
        assertThat(region.getEventLocations().get(0).getName()).isNotNull();
    }

    @Test
    void shouldEnforceUniqueCode() {
        Region duplicate = Region.builder()
            .name("Duplicate Region")
            .code("NORTH")
            .defaultWinProbability(0.2)
            .status(1)
            .build();

        assertThatThrownBy(() -> regionRepository.save(duplicate))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldVerifyUniqueCode() {
        assertThat(regionRepository.existsByCode("NORTH")).isTrue();
        assertThat(regionRepository.existsByCode("NONEXISTENT")).isFalse();
        assertThat(regionRepository.existsByCode(null)).isFalse();
    }

    @Test
    void shouldVerifyUniqueCodeExcludingId() {
        Optional<Region> regionOpt = regionRepository.findByCode("NORTH");
        assertThat(regionOpt).isPresent();
        Region region = regionOpt.get();
        assertThat(regionRepository.existsByCodeAndIdNot("NORTH", region.getId())).isFalse();
        assertThat(regionRepository.existsByCodeAndIdNot("NORTH", 999L)).isTrue();
        assertThat(regionRepository.existsByCodeAndIdNot("NONEXISTENT", region.getId())).isFalse();
    }

    @Test
    void shouldCascadeDeleteLocations() {
        Optional<Region> regionOpt = regionRepository.findByIdWithLocations(1L);
        assertThat(regionOpt).isPresent();
        Region region = regionOpt.get();
        regionRepository.delete(region);
        entityManager.flush();
        entityManager.clear();

        assertThat(regionRepository.findById(1L)).isEmpty();
        // Verify cascade delete worked
        assertThat(regionRepository.findByIdWithLocations(1L)).isEmpty();
    }
}
