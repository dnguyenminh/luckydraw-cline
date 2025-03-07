package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

@Entity
@Table(name = "golden_hours")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
public class GoldenHour extends AbstractStatusAwareEntity {

    private static final long serialVersionUID = 1L;

    public GoldenHour() {
        super();
        this.status = STATUS_INACTIVE;
        this.totalUses = 0;
        this.pointsMultiplier = 1.0;
        this.winProbabilityMultiplier = 1.0;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id")
    private EventLocation eventLocation;

    @Column(name = "name")
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "win_probability")
    private Double winProbability;

    @Column(name = "win_probability_multiplier")
    private Double winProbabilityMultiplier;

    @Column(name = "points_multiplier")
    private Double pointsMultiplier;

    @Column(name = "daily_limit")
    private Integer dailyLimit;

    @Column(name = "total_uses")
    private Integer totalUses;

    @Column(name = "metadata")
    private String metadata;

    // Event relationship methods
    @Deprecated
    public Event getEvent() {
        return eventLocation != null ? eventLocation.getEvent() : null;
    }

    @Deprecated
    public void setEvent(Event event) {
        if (event != null && !event.getEventLocations().isEmpty()) {
            setEventLocation(event.getDefaultLocation());
        }
    }

    // EventLocation relationship methods
    public void setEventLocation(EventLocation location) {
        if (this.eventLocation != null && this.eventLocation.getGoldenHours().contains(this)) {
            this.eventLocation.getGoldenHours().remove(this);
        }
        this.eventLocation = location;
        if (location != null && !location.getGoldenHours().contains(this)) {
            location.getGoldenHours().add(this);
        }
    }

    // Usage tracking methods
    public void setDailyLimit(int limit) {
        this.dailyLimit = limit;
    }

    public void incrementUses() {
        if (totalUses == null) {
            totalUses = 0;
        }
        totalUses++;
    }

    public Integer getTotalUses() {
        if (totalUses == null) {
            totalUses = 0;
        }
        return totalUses;
    }

    public void resetUses() {
        totalUses = 0;
    }

    // Multiplier methods
    public void setPointsMultiplier(double multiplier) {
        this.pointsMultiplier = multiplier;
    }

    public Double getPointsMultiplier() {
        return pointsMultiplier != null ? pointsMultiplier : 1.0;
    }

    public void setWinProbabilityMultiplier(double multiplier) {
        this.winProbabilityMultiplier = multiplier;
    }

    public Double getWinProbabilityMultiplier() {
        return winProbabilityMultiplier != null ? winProbabilityMultiplier : 1.0;
    }

    // Win probability methods
    public Double getWinProbability() {
        double baseProb = winProbability != null ? winProbability : 
                         (eventLocation != null ? eventLocation.getDefaultWinProbability() : 0.0);
        return baseProb * getWinProbabilityMultiplier();
    }

    // Activation methods
    private boolean isStatusActive() {
        return super.isActive() && eventLocation != null && eventLocation.isActive();
    }

    private boolean isWithinTimeWindow(LocalDateTime at) {
        if (startTime == null || endTime == null || at == null) {
            return false;
        }
        return !at.isBefore(startTime) && !at.isAfter(endTime);
    }

    private boolean isWithinUsageLimit() {
        if (dailyLimit == null) {
            return true;
        }
        return getTotalUses() < dailyLimit;
    }

    public boolean isActive(LocalDateTime at) {
        return isStatusActive() && isWithinTimeWindow(at) && isWithinUsageLimit();
    }

    public boolean isCurrentlyActive() {
        return isActive(LocalDateTime.now());
    }

    @Override
    public boolean isActive() {
        return isCurrentlyActive();
    }

    @Override
    public String toString() {
        return String.format("GoldenHour[id=%d, code=%s, location=%s, start=%s, end=%s, probability=%.2f, multiplier=%.2f]",
            id,
            code,
            eventLocation != null ? eventLocation.getCode() : "null",
            startTime,
            endTime,
            getWinProbability(),
            getWinProbabilityMultiplier()
        );
    }
}
