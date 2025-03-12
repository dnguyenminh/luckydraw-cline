package vn.com.fecredit.app.dto.projection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class HourlyStatsProjectionTest {

    @Autowired
    private SpinHistoryRepository spinHistoryRepository;

    @Autowired
    private EventLocationRepository locationRepository;

    @Test
    void shouldCalculateHourlyStats() {
        // Given
        EventLocation location = EventLocation.builder()
                .name("Test Location")
                .dailySpinLimit(100)
                .winProbabilityMultiplier(0.5)
                .status(EntityStatus.ACTIVE)
                .build();
        location = locationRepository.save(location);

        // Create spins at different hours
        LocalDateTime now = LocalDateTime.now();
        
        // Hour 10: 2 spins, 1 win
        createSpin(location, now.with(LocalTime.of(10, 0)), true);
        createSpin(location, now.with(LocalTime.of(10, 30)), false);
        
        // Hour 11: 3 spins, 2 wins
        createSpin(location, now.with(LocalTime.of(11, 0)), true);
        createSpin(location, now.with(LocalTime.of(11, 30)), true);
        createSpin(location, now.with(LocalTime.of(11, 45)), false);
        
        // Hour 12: 1 spin, 0 wins
        createSpin(location, now.with(LocalTime.of(12, 0)), false);

        // When
        List<HourlyStatsProjection> stats = spinHistoryRepository.findHourlyStatsByLocation(location);

        // Then
        assertThat(stats).hasSize(3);
        
        // Verify hour 10 stats
        HourlyStatsProjection hour10Stats = findStatsByHour(stats, 10);
        assertThat(hour10Stats).isNotNull();
        assertThat(hour10Stats.getTotalSpins()).isEqualTo(2L);
        assertThat(hour10Stats.getWinningSpins()).isEqualTo(1L);
        assertThat(hour10Stats.getWinRate()).isEqualTo(0.5);

        // Verify hour 11 stats
        HourlyStatsProjection hour11Stats = findStatsByHour(stats, 11);
        assertThat(hour11Stats).isNotNull();
        assertThat(hour11Stats.getTotalSpins()).isEqualTo(3L);
        assertThat(hour11Stats.getWinningSpins()).isEqualTo(2L);
        assertThat(hour11Stats.getWinRate()).isCloseTo(0.667, within(0.001));

        // Verify hour 12 stats
        HourlyStatsProjection hour12Stats = findStatsByHour(stats, 12);
        assertThat(hour12Stats).isNotNull();
        assertThat(hour12Stats.getTotalSpins()).isEqualTo(1L);
        assertThat(hour12Stats.getWinningSpins()).isEqualTo(0L);
        assertThat(hour12Stats.getWinRate()).isEqualTo(0.0);
    }

    @Test
    void shouldHandleEmptyStats() {
        // Given
        EventLocation location = EventLocation.builder()
                .name("Empty Location")
                .dailySpinLimit(100)
                .winProbabilityMultiplier(0.5)
                .status(EntityStatus.ACTIVE)
                .build();
        location = locationRepository.save(location);

        // When
        List<HourlyStatsProjection> stats = spinHistoryRepository.findHourlyStatsByLocation(location);

        // Then
        assertThat(stats).isEmpty();
    }

    private SpinHistory createSpin(EventLocation location, LocalDateTime spinTime, boolean won) {
        SpinHistory spin = SpinHistory.builder()
                .location(location)
                .spinTime(spinTime)
                .won(won)
                .build();
        return spinHistoryRepository.save(spin);
    }

    private HourlyStatsProjection findStatsByHour(List<HourlyStatsProjection> stats, int hour) {
        return stats.stream()
                .filter(stat -> stat.getHour() == hour)
                .findFirst()
                .orElse(null);
    }

    private static org.assertj.core.data.Offset<Double> within(double precision) {
        return org.assertj.core.data.Offset.offset(precision);
    }
}
