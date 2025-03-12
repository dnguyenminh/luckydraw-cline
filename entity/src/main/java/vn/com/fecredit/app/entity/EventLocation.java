package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing a physical location where an event takes place.
 * Each event location is associated with a specific event and region, and can have its own
 * set of rewards, golden hours, and participants.
 * <p>
 * Event locations allow for region-specific customization of event parameters such as
 * win probabilities, spin limits, and available rewards.
 */
@Entity
@Table(name = "event_locations")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EventLocation extends AbstractStatusAwareEntity {

    /**
     * The name of the event location displayed to users.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Unique code identifier for the location, used in APIs and references.
     */
    @Column(nullable = false, length = 20, unique = true)
    private String code;

    /**
     * Detailed description of the event location.
     */
    @Column(name = "description")
    private String description;

    /**
     * The initial number of spins allocated to participants when they join at this location.
     * If null, the event's initial spins value is used.
     */
    @Column(name = "initial_spins")
    private Integer initialSpins;

    /**
     * The maximum number of spins a participant can use per day at this location.
     * If null, the event's daily spin limit is used.
     */
    @Column(name = "daily_spin_limit")
    private Integer dailySpinLimit;

    /**
     * The default probability of winning a reward when spinning at this location.
     * If null, the region's or event's default win probability is used.
     */
    @Column(name = "default_win_probability")
    private Double defaultWinProbability;

    /**
     * Additional metadata stored as a JSON string for extensibility.
     */
    @Column(name = "metadata")
    private String metadata;

    /**
     * The event that this location is part of.
     * This establishes the many-to-one relationship with the Event entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private Event event;

    /**
     * The region that this location is associated with.
     * This establishes the many-to-one relationship with the Region entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    @ToString.Exclude
    private Region region;

    /**
     * The collection of participant event records associated with this location.
     * This establishes the one-to-many relationship with the ParticipantEvent entity.
     */
    @OneToMany(mappedBy = "eventLocation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<ParticipantEvent> participantEvents = new LinkedHashSet<>();

    /**
     * The collection of rewards available at this location.
     * This establishes the one-to-many relationship with the Reward entity.
     */
    @OneToMany(mappedBy = "eventLocation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<Reward> rewards = new LinkedHashSet<>();

    /**
     * The collection of golden hours scheduled at this location.
     * This establishes the one-to-many relationship with the GoldenHour entity.
     */
    @OneToMany(mappedBy = "eventLocation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<GoldenHour> goldenHours = new LinkedHashSet<>();

    /**
     * The collection of spin history records associated with this location.
     * This establishes the one-to-many relationship with the SpinHistory entity.
     */
    @OneToMany(mappedBy = "eventLocation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<SpinHistory> spinHistories = new LinkedHashSet<>();

    /**
     * Custom logic executed when this entity is activated.
     * Ensures that related entities (event and region) are also active.
     * 
     * @throws IllegalStateException if the event or region is inactive
     */
    @Override
    protected void onActivate() {
        if (event == null || !event.isActive()) {
            throw new IllegalStateException("Cannot activate location for inactive event");
        }
        if (region == null || !region.isActive()) {
            throw new IllegalStateException("Cannot activate location for inactive region");
        }
    }

    /**
     * Custom logic executed when this entity is deactivated.
     * Prevents deactivation if there are any active participants at this location.
     * 
     * @throws IllegalStateException if there are active participants
     */
    @Override
    protected void onDeactivate() {
        if (!participantEvents.isEmpty() && 
            participantEvents.stream().anyMatch(pe -> pe.isActive())) {
            throw new IllegalStateException("Cannot deactivate location with active participants");
        }
    }

    /**
     * Validates the state of this entity before persistence or update operations.
     * Ensures that required relationships are established and numeric values are valid.
     * 
     * @throws IllegalStateException if validation fails
     */
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

    /**
     * Gets the effective initial spins value for this location.
     * If this location has a specific value, it is used; otherwise, the event's value is used.
     * 
     * @return the effective initial spins value
     */
    public Integer getEffectiveInitialSpins() {
        return initialSpins != null ? initialSpins : event.getInitialSpins();
    }

    /**
     * Gets the effective daily spin limit for this location.
     * If this location has a specific value, it is used; otherwise, the event's value is used.
     * 
     * @return the effective daily spin limit
     */
    public Integer getEffectiveDailySpinLimit() {
        return dailySpinLimit != null ? dailySpinLimit : event.getDailySpinLimit();
    }

    /**
     * Gets the effective win probability for this location.
     * Uses a cascading fallback mechanism:
     * 1. This location's win probability if set
     * 2. The region's win probability if set
     * 3. The event's win probability as a final fallback
     * 
     * @return the effective win probability
     */
    public Double getEffectiveWinProbability() {
        if (defaultWinProbability != null) return defaultWinProbability;
        if (region != null && region.getDefaultWinProbability() != null) {
            return region.getDefaultWinProbability();
        }
        return event.getDefaultWinProbability();
    }
}
