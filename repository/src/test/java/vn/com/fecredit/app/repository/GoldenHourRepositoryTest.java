package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.BaseRepositoryTest;

class GoldenHourRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private GoldenHourRepository goldenHourRepository;

    private Event event;
    private EventLocation location;
    private Region region;

    @BeforeEach
    void setUp() {
        // Create and save test Region
        region = Region.builder()
            .name("Test Region")
            .code("TEST_REGION")
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(region);

        // Create and save test Event
        event = Event.builder()
            .name("Test Event")
            .code("TEST_EVENT")
            .startTime(LocalDateTime.now().minusDays(1))
            .endTime(LocalDateTime.now().plusDays(30))
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(event);

        // Create and save test EventLocation
        location = EventLocation.builder()
            .event(event)
            .region(region)
            .name("Test Location")
            .code("TEST_LOC")
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(location);
    }

    @Test
    void findActive_ShouldReturnCurrentGoldenHours() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        GoldenHour active = GoldenHour.builder()
            .eventLocation(location)
            .name("Active Golden Hour")
            .startTime(now.minusMinutes(30))
            .endTime(now.plusMinutes(30))
            .winProbability(0.2)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();

        GoldenHour future = GoldenHour.builder()
            .eventLocation(location)
            .name("Future Golden Hour")
            .startTime(now.plusHours(1))
            .endTime(now.plusHours(2))
            .winProbability(0.2)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();

        persistAndFlush(active);
        persistAndFlush(future);

        // When
        List<GoldenHour> activeHours = goldenHourRepository.findActive(
            location,
            AbstractStatusAwareEntity.STATUS_ACTIVE,
            now
        );

        // Then
        assertThat(activeHours).hasSize(1);
        assertEquals("Active Golden Hour", activeHours.get(0).getName());
    }

    @Test
    void hasOverlappingHours_ShouldDetectOverlaps() {
        // Given
        LocalDateTime baseTime = LocalDateTime.now();
        GoldenHour existing = GoldenHour.builder()
            .eventLocation(location)
            .name("Existing Golden Hour")
            .startTime(baseTime.plusHours(1))
            .endTime(baseTime.plusHours(2))
            .winProbability(0.2)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(existing);

        // Test overlapping start
        boolean hasOverlap1 = goldenHourRepository.hasOverlappingHours(
            location,
            AbstractStatusAwareEntity.STATUS_ACTIVE,
            baseTime.plusMinutes(30),
            baseTime.plusHours(1).plusMinutes(30)
        );

        // Test overlapping end
        boolean hasOverlap2 = goldenHourRepository.hasOverlappingHours(
            location,
            AbstractStatusAwareEntity.STATUS_ACTIVE,
            baseTime.plusHours(1).plusMinutes(30),
            baseTime.plusHours(2).plusMinutes(30)
        );

        // Test completely contained
        boolean hasOverlap3 = goldenHourRepository.hasOverlappingHours(
            location,
            AbstractStatusAwareEntity.STATUS_ACTIVE,
            baseTime.plusHours(1).plusMinutes(15),
            baseTime.plusHours(1).plusMinutes(45)
        );

        // Test non-overlapping
        boolean hasOverlap4 = goldenHourRepository.hasOverlappingHours(
            location,
            AbstractStatusAwareEntity.STATUS_ACTIVE,
            baseTime.plusHours(3),
            baseTime.plusHours(4)
        );

        // Then
        assertTrue(hasOverlap1, "Should detect overlap at start");
        assertTrue(hasOverlap2, "Should detect overlap at end");
        assertTrue(hasOverlap3, "Should detect contained interval");
        assertFalse(hasOverlap4, "Should not detect non-overlapping interval");
    }

    @Test
    void findExpired_ShouldReturnPastGoldenHours() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireBefore = now.minusHours(1);
        
        GoldenHour expired1 = GoldenHour.builder()
            .eventLocation(location)
            .name("Expired Hour 1")
            .startTime(now.minusHours(3))
            .endTime(now.minusHours(2))
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();

        GoldenHour expired2 = GoldenHour.builder()
            .eventLocation(location)
            .name("Expired Hour 2")
            .startTime(now.minusHours(5))
            .endTime(now.minusHours(4))
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();

        GoldenHour active = GoldenHour.builder()
            .eventLocation(location)
            .name("Active Hour")
            .startTime(now.minusMinutes(30))
            .endTime(now.plusMinutes(30))
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();

        persistAndFlush(expired1);
        persistAndFlush(expired2);
        persistAndFlush(active);

        // When
        List<GoldenHour> expiredHours = goldenHourRepository.findExpired(
            expireBefore,
            AbstractStatusAwareEntity.STATUS_ACTIVE
        );

        // Then
        assertThat(expiredHours).hasSize(2);
        assertTrue(expiredHours.stream().allMatch(h -> h.getEndTime().isBefore(expireBefore)));
    }
}
