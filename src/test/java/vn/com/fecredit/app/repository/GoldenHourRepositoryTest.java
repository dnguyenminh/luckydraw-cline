package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class GoldenHourRepositoryTest {

    @Autowired
    private GoldenHourRepository goldenHourRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RewardRepository rewardRepository;

    private Event event;
    private Reward reward;
    private GoldenHour morningHour;
    private GoldenHour eveningHour;
    private GoldenHour inactiveHour;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setCode("TEST-EVENT");
        event.setName("Test Event");
        event.setStartDate(now.minusDays(1));
        event.setEndDate(now.plusDays(1));
        event.setIsActive(true);
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        event = eventRepository.save(event);

        reward = new Reward();
        reward.setEvent(event);
        reward.setName("Test Reward");
        reward.setQuantity(10);
        reward.setRemainingQuantity(5);
        reward.setIsActive(true);
        reward.setCreatedAt(now);
        reward.setUpdatedAt(now);
        reward = rewardRepository.save(reward);

        morningHour = GoldenHour.builder()
            .event(event)
            .reward(reward)
            .name("Morning Hour")
            .startTime(now.withHour(9).withMinute(0))
            .endTime(now.withHour(12).withMinute(0))
            .multiplier(2.0)
            .isActive(true)
            .build();
        morningHour = goldenHourRepository.save(morningHour);

        eveningHour = GoldenHour.builder()
            .event(event)
            .reward(reward)
            .name("Evening Hour")
            .startTime(now.withHour(22).withMinute(0))
            .endTime(now.plusDays(1).withHour(1).withMinute(0))  // Cross midnight
            .multiplier(3.0)
            .isActive(true)
            .build();
        eveningHour = goldenHourRepository.save(eveningHour);

        inactiveHour = GoldenHour.builder()
            .event(event)
            .reward(reward)
            .name("Inactive Hour")
            .startTime(now.withHour(15).withMinute(0))
            .endTime(now.withHour(18).withMinute(0))
            .multiplier(1.5)
            .isActive(false)
            .build();
        inactiveHour = goldenHourRepository.save(inactiveHour);
    }

    @Test
    void findByEventIdAndIsActiveTrue_ShouldReturnOnlyActiveHours() {
        List<GoldenHour> activeHours = goldenHourRepository.findByEventIdAndIsActiveTrue(event.getId());

        assertThat(activeHours).hasSize(2)
            .extracting("name")
            .containsExactlyInAnyOrder("Morning Hour", "Evening Hour");
    }

    @Test
    void findByRewardIdAndIsActiveTrue_ShouldReturnActiveHour() {
        List<GoldenHour> activeHours = goldenHourRepository.findByRewardIdAndIsActiveTrue(reward.getId());

        // Expecting at least one active golden hour
        assertThat(activeHours).isNotEmpty();
        // For example, check the first returned entity
        GoldenHour goldenHour = activeHours.get(0);
        assertThat(goldenHour.isActive()).isTrue();
        assertThat(goldenHour.getReward().getId()).isEqualTo(reward.getId());
    }

    @Test
    void findActiveGoldenHour_ShouldReturnHour_WhenWithinTimeRange() {
        LocalDateTime testTime = now.withHour(10).withMinute(30);
        List<GoldenHour> hours = goldenHourRepository.findByEventIdAndIsActiveTrue(event.getId());
        Optional<GoldenHour> found = hours.stream()
            .filter(h -> isTimeInRange(testTime, h.getStartTime(), h.getEndTime()))
            .findFirst();

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Morning Hour");
    }

    private boolean isTimeInRange(LocalDateTime time, LocalDateTime start, LocalDateTime end) {
        return !time.isBefore(start) && !time.isAfter(end);
    }

    @Test
    void findActiveGoldenHour_ShouldReturnHour_WhenWithinCrossMidnightRange() {
        LocalDateTime testTime = now.withHour(23).withMinute(30);
        Optional<GoldenHour> found = goldenHourRepository.findActiveGoldenHour(
            event.getId(),
            testTime
        );

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Evening Hour");
    }

    @Test
    void updateStatus_ShouldUpdateActiveStatus() {
        int updated = goldenHourRepository.updateStatus(morningHour.getId(), false);

        assertThat(updated).isEqualTo(1);
        Optional<GoldenHour> found = goldenHourRepository.findById(morningHour.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isActive()).isFalse();
    }

    @Test
    void findByIdWithDetails_ShouldReturnHourWithRelatedEntities() {
        Optional<GoldenHour> found = goldenHourRepository.findByIdWithDetails(morningHour.getId());

        assertThat(found).isPresent();
        GoldenHour hour = found.get();
        assertThat(hour.getEvent()).isNotNull();
        assertThat(hour.getReward()).isNotNull();
        assertThat(hour.getName()).isEqualTo("Morning Hour");
    }

    @Test
    void isGoldenHourActive_ShouldReturnTrue_WhenTimeIsWithinActiveHour() {
        LocalDateTime testTime = now.withHour(10).withMinute(30);
        boolean isActive = goldenHourRepository.isGoldenHourActive(event.getId(), testTime);

        assertThat(isActive).isTrue();
    }

    @Test
    void isGoldenHourActive_ShouldReturnFalse_WhenNoActiveHourExists() {
        LocalDateTime testTime = now.withHour(14).withMinute(30);
        boolean isActive = goldenHourRepository.isGoldenHourActive(event.getId(), testTime);

        assertThat(isActive).isFalse();
    }

    @Test
    void findActiveGoldenHoursOrdered_ShouldReturnHoursOrderedByStartTime() {
        List<GoldenHour> hours = goldenHourRepository.findActiveGoldenHoursOrdered(event.getId());

        assertThat(hours).hasSize(2);
        assertThat(hours.get(0).getName()).isEqualTo("Morning Hour");
        assertThat(hours.get(1).getName()).isEqualTo("Evening Hour");
    }

    @Test
    void existsByEventIdAndName_ShouldReturnTrue_WhenExists() {
        boolean exists = goldenHourRepository.existsByEventIdAndName(
            event.getId(),
            "Morning Hour"
        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEventIdAndName_ShouldReturnFalse_WhenNotExists() {
        boolean exists = goldenHourRepository.existsByEventIdAndName(
            event.getId(),
            "Non-existent Hour"
        );

        assertThat(exists).isFalse();
    }
}

