package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "golden_hours")
public class GoldenHour extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "probability_multiplier", nullable = false)
    private Double probabilityMultiplier;

    /**
     * Check if golden hour is currently active
     */
    public boolean isActive() {
        if (!super.getStatus().isActive()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return startTime.isBefore(now) && endTime.isAfter(now);
    }

    /**
     * Check if golden hour is active at a specific time
     */
    public boolean isActive(LocalDateTime checkTime) {
        if (!super.getStatus().isActive()) {
            return false;
        }

        return startTime.isBefore(checkTime) && endTime.isAfter(checkTime);
    }

    /**
     * Check if golden hour is upcoming
     */
    public boolean isUpcoming() {
        return super.getStatus().isActive() && startTime.isAfter(LocalDateTime.now());
    }

    /**
     * Check if golden hour has expired
     */
    public boolean isExpired() {
        return endTime.isBefore(LocalDateTime.now());
    }

    /**
     * Get probability multiplier for rewards
     */
    public double getProbabilityMultiplier() {
        return probabilityMultiplier != null ? probabilityMultiplier : 1.0;
    }
}
