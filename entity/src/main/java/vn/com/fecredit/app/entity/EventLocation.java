package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event_locations")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EventLocation extends AbstractStatusAwareEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "initial_spins")
    private Integer initialSpins;

    @Column(name = "daily_spin_limit")
    private Integer dailySpinLimit;

    @Column(name = "default_win_probability")
    private Double defaultWinProbability;

    @Column(name = "metadata")
    private String metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    @ToString.Exclude
    private Region region;

    @OneToMany(mappedBy = "eventLocation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ParticipantEvent> participantEvents = new ArrayList<>();

    @OneToMany(mappedBy = "eventLocation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Reward> rewards = new ArrayList<>();

    @OneToMany(mappedBy = "eventLocation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<GoldenHour> goldenHours = new ArrayList<>();

    @OneToMany(mappedBy = "eventLocation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<SpinHistory> spinHistories = new ArrayList<>();

    @Override
    protected void onActivate() {
        if (event == null || !event.isActive()) {
            throw new IllegalStateException("Cannot activate location for inactive event");
        }
        if (region == null || !region.isActive()) {
            throw new IllegalStateException("Cannot activate location for inactive region");
        }
    }

    @Override
    protected void onDeactivate() {
        if (!participantEvents.isEmpty() && 
            participantEvents.stream().anyMatch(pe -> pe.isActive())) {
            throw new IllegalStateException("Cannot deactivate location with active participants");
        }
    }

    @PrePersist
    @PreUpdate
    protected void validateState() {
        if (code != null) {
            code = code.toUpperCase();
        }
        
        if (initialSpins != null && initialSpins < 0) {
            throw new IllegalStateException("Initial spins cannot be negative");
        }
        
        if (dailySpinLimit != null && dailySpinLimit < 0) {
            throw new IllegalStateException("Daily spin limit cannot be negative");
        }
        
        if (defaultWinProbability != null && 
            (defaultWinProbability < 0 || defaultWinProbability > 1)) {
            throw new IllegalStateException("Win probability must be between 0 and 1");
        }

        if (event == null) {
            throw new IllegalStateException("Event is required");
        }

        if (region == null) {
            throw new IllegalStateException("Region is required");
        }
    }

    public Integer getEffectiveInitialSpins() {
        return initialSpins != null ? initialSpins : event.getInitialSpins();
    }

    public Integer getEffectiveDailySpinLimit() {
        return dailySpinLimit != null ? dailySpinLimit : event.getDailySpinLimit();
    }

    public Double getEffectiveWinProbability() {
        if (defaultWinProbability != null) return defaultWinProbability;
        if (region != null && region.getDefaultWinProbability() != null) {
            return region.getDefaultWinProbability();
        }
        return event.getDefaultWinProbability();
    }
}
