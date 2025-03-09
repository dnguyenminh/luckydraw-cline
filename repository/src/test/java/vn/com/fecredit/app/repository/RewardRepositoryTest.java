package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.BaseRepositoryTest;

class RewardRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private RewardRepository rewardRepository;

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
    void findByCode_ShouldReturnReward_WhenExists() {
        // Given
        Reward reward = Reward.builder()
            .eventLocation(location)
            .name("Test Reward")
            .code("TEST_REWARD")
            .points(100)
            .pointsRequired(50)
            .totalQuantity(100)
            .remainingQuantity(100)
            .dailyLimit(10)
            .winProbability(0.1)
            .validFrom(LocalDateTime.now().minusDays(1))
            .validUntil(LocalDateTime.now().plusDays(30))
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(reward);

        // When
        Optional<Reward> found = rewardRepository.findByCode("TEST_REWARD");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test Reward", found.get().getName());
        assertEquals(100, found.get().getPoints());
    }

    @Test
    void findAvailable_ShouldReturnOnlyAvailableRewards() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        Reward available = Reward.builder()
            .eventLocation(location)
            .name("Available Reward")
            .code("AVAILABLE")
            .points(100)
            .pointsRequired(50)
            .totalQuantity(100)
            .remainingQuantity(100)
            .dailyLimit(10)
            .dailyCount(5)
            .winProbability(0.1)
            .validFrom(now.minusDays(1))
            .validUntil(now.plusDays(30))
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();

        Reward expired = Reward.builder()
            .eventLocation(location)
            .name("Expired Reward")
            .code("EXPIRED")
            .points(100)
            .pointsRequired(50)
            .totalQuantity(100)
            .remainingQuantity(100)
            .validFrom(now.minusDays(30))
            .validUntil(now.minusDays(1))
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();

        Reward outOfStock = Reward.builder()
            .eventLocation(location)
            .name("Out of Stock")
            .code("OUT_OF_STOCK")
            .points(100)
            .pointsRequired(50)
            .totalQuantity(100)
            .remainingQuantity(0)
            .validFrom(now.minusDays(1))
            .validUntil(now.plusDays(30))
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();

        persistAndFlush(available);
        persistAndFlush(expired);
        persistAndFlush(outOfStock);

        // When
        List<Reward> availableRewards = rewardRepository.findAvailable(
            location, 
            AbstractStatusAwareEntity.STATUS_ACTIVE,
            now
        );

        // Then
        assertThat(availableRewards).hasSize(1);
        assertEquals("Available Reward", availableRewards.get(0).getName());
    }

    @Test
    void findActiveByLocation_ShouldPaginateResults() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 15; i++) {
            Reward reward = Reward.builder()
                .eventLocation(location)
                .name("Reward " + i)
                .code("REWARD_" + i)
                .points(100)
                .validFrom(now.minusDays(1))
                .validUntil(now.plusDays(30))
                .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
                .build();
            persistAndFlush(reward);
        }

        // When
        Page<Reward> firstPage = rewardRepository.findActiveByLocation(
            location,
            now,
            PageRequest.of(0, 10)
        );

        Page<Reward> secondPage = rewardRepository.findActiveByLocation(
            location,
            now,
            PageRequest.of(1, 10)
        );

        // Then
        assertEquals(10, firstPage.getContent().size());
        assertEquals(5, secondPage.getContent().size());
        assertEquals(15, firstPage.getTotalElements());
    }
}
