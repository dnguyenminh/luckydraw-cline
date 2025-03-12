package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.RegionDTO;
import vn.com.fecredit.app.dto.RegionDTO.*;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.exception.InvalidOperationException;
import vn.com.fecredit.app.service.RegionService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Sql(scripts = {"/schema-test.sql", "/data-test.sql"})
class RegionServiceIntegrationTest {

    @Autowired
    private RegionService regionService;

    @Test
    void shouldCreateRegion() {
        CreateRequest request = CreateRequest.builder()
            .name("Test Region")
            .code("TEST")
            .defaultWinProbability(0.3)
            .build();

        Response response = regionService.createRegion(request);
        
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Region");
        assertThat(response.getCode()).isEqualTo("TEST");
        assertThat(response.getDefaultWinProbability()).isEqualTo(0.3);
        assertThat(response.getStatus()).isEqualTo(1);
    }

    @Test
    void shouldNotCreateDuplicateCode() {
        CreateRequest request = CreateRequest.builder()
            .name("North Copy")
            .code("NORTH")
            .defaultWinProbability(0.3)
            .build();

        assertThatThrownBy(() -> regionService.createRegion(request))
            .isInstanceOf(InvalidOperationException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void shouldUpdateRegion() {
        Optional<Response> existing = regionService.getRegion(1L);
        assertThat(existing).isPresent();
        String originalCode = existing.get().getCode();

        UpdateRequest request = UpdateRequest.builder()
            .name("Updated North")
            .defaultWinProbability(0.4)
            .build();

        Response response = regionService.updateRegion(1L, request);
        
        assertThat(response.getName()).isEqualTo("Updated North");
        assertThat(response.getDefaultWinProbability()).isEqualTo(0.4);
        assertThat(response.getCode()).isEqualTo(originalCode); // Code should not change
    }

    @Test
    void shouldNotUpdateNonexistentRegion() {
        UpdateRequest request = UpdateRequest.builder()
            .name("Updated")
            .build();

        assertThatThrownBy(() -> regionService.updateRegion(999L, request))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldListRegionsWithPagination() {
        Page<Response> page = regionService.listRegions(PageRequest.of(0, 2));
        
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldListActiveRegions() {
        Page<Response> page = regionService.listActiveRegions(PageRequest.of(0, 10));
        
        assertThat(page.getContent())
            .isNotEmpty()
            .allMatch(r -> r.getStatus() == 1)
            .hasSize(3);
    }

    @Test
    void shouldActivateRegion() {
        regionService.activateRegion(3L); // Central Region is inactive in test data
        Optional<Response> region = regionService.getRegion(3L);
        
        assertThat(region).isPresent();
        assertThat(region.get().getStatus()).isEqualTo(1);
    }

    @Test
    void shouldDeactivateRegion() {
        // First deactivate all provinces and locations
        regionService.deactivateRegion(3L); // Use region with no active items
        Optional<Response> region = regionService.getRegion(3L);
        
        assertThat(region).isPresent();
        assertThat(region.get().getStatus()).isEqualTo(0);
    }

    @Test
    void shouldNotDeactivateRegionWithActiveProvinces() {
        assertThatThrownBy(() -> regionService.deactivateRegion(1L))
            .isInstanceOf(InvalidOperationException.class)
            .hasMessageContaining("active provinces");
    }

    @Test
    void shouldDeleteRegion() {
        regionService.deleteRegion(3L); // Central region has no active items
        assertThat(regionService.getRegion(3L)).isEmpty();
    }

    @Test
    void shouldNotDeleteRegionWithActiveItems() {
        assertThatThrownBy(() -> regionService.deleteRegion(1L))
            .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void shouldGetRegionSummaries() {
        List<Summary> summaries = regionService.getRegionSummaries();
        assertThat(summaries)
            .isNotEmpty()
            .allSatisfy(summary -> assertThat(summary).isNotNull());
    }

    @Test
    void shouldGetActiveRegionSummaries() {
        List<Summary> summaries = regionService.getActiveRegionSummaries();
        assertThat(summaries)
            .isNotEmpty()
            .hasSize(3); // 3 active regions in test data
    }

    @Test
    void shouldVerifyRegionExistsByCode() {
        assertThat(regionService.existsByCode("NORTH")).isTrue();
        assertThat(regionService.existsByCode("NONEXISTENT")).isFalse();
    }

    @Test
    void shouldFindRegionByCode() {
        Optional<Response> region = regionService.findByCode("NORTH");
        assertThat(region)
            .isPresent()
            .hasValueSatisfying(r -> {
                assertThat(r.getCode()).isEqualTo("NORTH");
                assertThat(r.getStatus()).isEqualTo(1);
            });
    }
}
