package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Participant;

import jakarta.validation.ConstraintViolationException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"/schema-test.sql", "/data-test.sql"})
class ProvinceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private RegionRepository regionRepository;

    private Region region;
    private Province hanoi;

    @BeforeEach
    void setUp() {
        region = Region.builder()
            .name("North Region")
            .code("NORTH")
            .defaultWinProbability(0.3)
            .status(1)
            .build();

        region = regionRepository.save(region);

        hanoi = Province.builder()
            .name("Hanoi")
            .code("HN")
            .defaultWinProbability(0.25)
            .region(region)
            .status(1)
            .build();
    }

    @Test
    void shouldSaveProvinceSuccessfully() {
        Province savedProvince = provinceRepository.save(hanoi);
        assertNotNull(savedProvince.getId());
        assertEquals("HN", savedProvince.getCode());
    }

    @Test
    void shouldThrowExceptionWhenCodeDuplicate() {
        provinceRepository.save(hanoi);
        Province duplicate = Province.builder()
            .name("Another Hanoi")
            .code("HN")
            .status(1)
            .build();

        assertThrows(Exception.class, () -> provinceRepository.save(duplicate));
    }

    @Test
    void shouldThrowExceptionWhenNameTooShort() {
        hanoi.setName("H");
        assertThrows(ConstraintViolationException.class, () -> 
            entityManager.persist(hanoi)
        );
    }

    @Test
    void shouldFindByCodeSuccessfully() {
        provinceRepository.save(hanoi);
        Optional<Province> found = provinceRepository.findByCode("HN");
        assertTrue(found.isPresent());
        assertEquals("Hanoi", found.get().getName());
    }

    @Test
    void shouldInheritRegionWinProbabilityWhenNotSet() {
        hanoi.setDefaultWinProbability(null);
        Province savedProvince = provinceRepository.save(hanoi);
        assertEquals(region.getDefaultWinProbability(), savedProvince.getEffectiveDefaultWinProbability());
    }

    @Test
    void shouldUseOwnWinProbabilityWhenSet() {
        Province savedProvince = provinceRepository.save(hanoi);
        assertEquals(0.25, savedProvince.getEffectiveDefaultWinProbability());
    }

    @Test
    void shouldHandleBidirectionalRelationshipsCorrectly() {
        Province savedProvince = provinceRepository.save(hanoi);
        
        Participant participant = Participant.builder()
            .name("Test Participant")
            .status(1)
            .build();
            
        savedProvince.addParticipant(participant);
        entityManager.flush();
        entityManager.clear();

        Province loaded = provinceRepository.findById(savedProvince.getId()).get();
        assertFalse(loaded.getParticipants().isEmpty());
        assertEquals("Test Participant", loaded.getParticipants().iterator().next().getName());
    }

    @Test
    void shouldReturnEmptyWhenCodeNotFound() {
        Optional<Province> notFound = provinceRepository.findByCode("NOT_EXIST");
        assertTrue(notFound.isEmpty());
    }

    @Test
    void shouldHandleNullRegionCorrectly() {
        hanoi.setRegion(null);
        Province savedProvince = provinceRepository.save(hanoi);
        assertNull(savedProvince.getRegion());
        assertEquals(0.25, savedProvince.getEffectiveDefaultWinProbability());
    }
}
