package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.EventLocation;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"/schema-test.sql", "/data-test.sql"})
class RegionRepositoryTest {

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

    private Region region;
    private Province province;
    private EventLocation eventLocation;

    @BeforeEach
    void setUp() {
        region = Region.builder()
            .name("North Region")
            .code("NORTH")
            .defaultWinProbability(0.3)
            .status(1)
            .build();

        region = regionRepository.save(region);

        province = Province.builder()
            .name("Hanoi")
            .code("HN")
            .defaultWinProbability(0.25)
            .status(1)
            .region(region)
            .build();

        eventLocation = EventLocation.builder()
            .name("Event Center 1")
            .code("EC1")
            .status(1)
            .region(region)
            .build();
    }

    @Test
    void shouldFindByCode() {
        Optional<Region> found = regionRepository.findByCode("NORTH");
        assertTrue(found.isPresent());
        assertEquals("North Region", found.get().getName());
    }

    @Test
    void shouldFindAllByStatus() {
        List<Region> activeRegions = regionRepository.findAllByStatus(1);
        assertFalse(activeRegions.isEmpty());
        assertTrue(activeRegions.stream().allMatch(r -> r.getStatus() == 1));
    }

    @Test
    void shouldPaginateByStatus() {
        Page<Region> page = regionRepository.findAllByStatus(1, PageRequest.of(0, 10));
        assertNotNull(page);
        assertTrue(page.getContent().stream().allMatch(r -> r.getStatus() == 1));
    }

    @Test
    void shouldCheckExistsByCode() {
        assertTrue(regionRepository.existsByCode("NORTH"));
        assertFalse(regionRepository.existsByCode("NONEXISTENT"));
    }

    @Test
    void shouldFindRegionsWithParticipants() {
        province = provinceRepository.save(province);
        // Add participant to province in test data

        List<Region> regions = regionRepository.findRegionsWithParticipants();
        assertFalse(regions.isEmpty());
    }

    @Test
    void shouldFindRegionsWithLocations() {
        eventLocation = eventLocationRepository.save(eventLocation);

        List<Region> regions = regionRepository.findRegionsWithLocations();
        assertFalse(regions.isEmpty());
        assertTrue(regions.stream()
            .anyMatch(r -> !r.getEventLocations().isEmpty()));
    }

    @Test
    void shouldFindByIdWithProvinces() {
        province = provinceRepository.save(province);

        Optional<Region> found = regionRepository.findByIdWithProvinces(region.getId());
        assertTrue(found.isPresent());
        assertFalse(found.get().getProvinces().isEmpty());
    }

    @Test
    void shouldFindByIdWithLocations() {
        eventLocation = eventLocationRepository.save(eventLocation);

        Optional<Region> found = regionRepository.findByIdWithLocations(region.getId());
        assertTrue(found.isPresent());
        assertFalse(found.get().getEventLocations().isEmpty());
    }

    @Test
    void shouldCheckForActiveProvinces() {
        province = provinceRepository.save(province);

        boolean hasActive = regionRepository.hasActiveProvinces(region.getId());
        assertTrue(hasActive);

        province.setStatus(0);
        province = provinceRepository.save(province);
        
        hasActive = regionRepository.hasActiveProvinces(region.getId());
        assertFalse(hasActive);
    }

    @Test
    void shouldCheckForActiveEventLocations() {
        eventLocation = eventLocationRepository.save(eventLocation);

        boolean hasActive = regionRepository.hasActiveEventLocations(region.getId());
        assertTrue(hasActive);

        eventLocation.setStatus(0);
        eventLocation = eventLocationRepository.save(eventLocation);
        
        hasActive = regionRepository.hasActiveEventLocations(region.getId());
        assertFalse(hasActive);
    }

    @Test
    void shouldCheckExistsByCodeAndIdNot() {
        Region otherRegion = Region.builder()
            .name("South Region")
            .code("SOUTH")
            .defaultWinProbability(0.3)
            .status(1)
            .build();
        otherRegion = regionRepository.save(otherRegion);

        assertTrue(regionRepository.existsByCodeAndIdNot("SOUTH", region.getId()));
        assertFalse(regionRepository.existsByCodeAndIdNot("SOUTH", otherRegion.getId()));
    }
}
