package vn.com.fecredit.app.entity;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

/**
 * Entity representing a special time period during an event when participants have increased chances of winning.
 * Golden hours can modify win probabilities and point rewards, creating excitement and engagement during specific times.
 * <p>
 * Each golden hour is associated with a specific event location, has a defined time window, and can have usage limits
 * to control how many times it can be used.
 */
@Entity
@Table(name = "golden_hours")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
public class GoldenHour extends AbstractStatusAwareEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor that initializes the golden hour with inactive status and default multipliers.
     */
    public GoldenHour() {
        super();
        this.status = STATUS_INACTIVE;
        this.totalUses = 0;
        this.pointsMultiplier = 1.0;
        this.winProbabilityMultiplier = 1.0;
    }

    /**
     * Unique identifier for the golden hour.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The event location where this golden hour is active.
     * This establishes the many-to-one relationship with the EventLocation entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id")
    private EventLocation eventLocation;

    /**
     * The name of the golden hour displayed to users.
     */
    @Column(name = "name")
    private String name;

    /**
     * Unique code identifier for the golden hour, used in APIs and references.
     */
    @Column(name = "code", unique = true)
    private String code;

    /**
     * Detailed description of the golden hour.
     */
    @Column(name = "description")
    private String description;

    /**
     * The date and time when the golden hour starts.
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * The date and time when the golden hour ends.
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * The absolute win probability during this golden hour.
     * If set, this overrides the location's default win probability.
     * If null, the location's win probability is used and multiplied by the win probability multiplier.
     */
    @Column(name = "win_probability")
    private Double winProbability;

    /**
     * The multiplier applied to the base win probability during this golden hour.
     * Default is 1.0 (no change).
     */
    @Column(name = "win_probability_multiplier")
    private Double winProbabilityMultiplier;

    /**
     * The multiplier applied to points earned during this golden hour.
     * Default is 1.0 (no change).
     */
    @Column(name = "points_multiplier")
    private Double pointsMultiplier;

    /**
     * The maximum number of times this golden hour can be used per day.
     */
    @Column(name = "daily_limit")
    private Integer dailyLimit;

    /**
     * The count of how many times this golden hour has been used.
     */
    @Column(name = "total_uses")
    private Integer totalUses;

    /**
     * Additional metadata stored as a JSON string for extensibility.
     */
    @Column(name = "metadata")
    private String metadata;

    /**
     * The collection of spin history records associated with this golden hour.
     * This establishes the one-to-many relationship with the SpinHistory entity.
     */
    @OneToMany(mappedBy = "goldenHour", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp DESC")
    @Builder.Default
    private Set<SpinHistory> spinHistories = new LinkedHashSet<>();

    /**
     * Gets the event associated with this golden hour through its event location.
     * 
     * @return the event, or null if no event location is set
     * @deprecated Use getEventLocation().getEvent() instead
     */
    @Deprecated
    public Event getEvent() {
        return eventLocation != null ? eventLocation.getEvent() : null;
    }

    /**
     * Sets the event for this golden hour by setting its event location to the event's default location.
     * 
     * @param event the event to associate with this golden hour
     * @deprecated Use setEventLocation(EventLocation) instead
     */
    @Deprecated
    public void setEvent(Event event) {
        if (event != null && !event.getEventLocations().isEmpty()) {
            setEventLocation(event.getDefaultLocation());
        }
    }

    /**
     * Sets the event location for this golden hour, maintaining the bidirectional relationship.
     * Removes this golden hour from its previous event location (if any) and adds it to the new one.
     * 
     * @param location the event location to associate with this golden hour
     */
    public void setEventLocation(EventLocation location) {
        if (this.eventLocation != null && this.eventLocation.getGoldenHours().contains(this)) {
            this.eventLocation.getGoldenHours().remove(this);
        }
        this.eventLocation = location;
        if (location != null && !location.getGoldenHours().contains(this)) {
            location.getGoldenHours().add(this);
        }
    }

    /**
     * Sets the daily usage limit for this golden hour.
     * 
     * @param limit the maximum number of times this golden hour can be used per day
     */
    public void setDailyLimit(int limit) {
        this.dailyLimit = limit;
    }

    /**
     * Increments the total uses count for this golden hour.
     * This is called when a participant spins during this golden hour.
     */
    public void incrementUses() {
        if (totalUses == null) {
            totalUses = 0;
        }
        totalUses++;
    }

    /**
     * Gets the total uses count with null safety.
     * 
     * @return the number of times this golden hour has been used, or 0 if null
     */
    public Integer getTotalUses() {
        if (totalUses == null) {
            totalUses = 0;
        }
        return totalUses;
    }

    /**
     * Resets the total uses count to zero.
     * This is typically called at the start of a new day.
     */
    public void resetUses() {
        totalUses = 0;
    }

    /**
     * Sets the points multiplier for this golden hour.
     * 
     * @param multiplier the multiplier to apply to points earned during this golden hour
     */
    public void setPointsMultiplier(double multiplier) {
        this.pointsMultiplier = multiplier;
    }

    /**
     * Gets the points multiplier with null safety.
     * 
     * @return the points multiplier, or 1.0 if null
     */
    public Double getPointsMultiplier() {
        return pointsMultiplier != null ? pointsMultiplier : 1.0;
    }

    /**
     * Sets the win probability multiplier for this golden hour.
     * 
     * @param multiplier the multiplier to apply to the base win probability during this golden hour
     */
    public void setWinProbabilityMultiplier(double multiplier) {
        this.winProbabilityMultiplier = multiplier;
    }

    /**
     * Gets the win probability multiplier with null safety.
     * 
     * @return the win probability multiplier, or 1.0 if null
     */
    public Double getWinProbabilityMultiplier() {
        return winProbabilityMultiplier != null ? winProbabilityMultiplier : 1.0;
    }

    /**
     * Calculates the effective win probability during this golden hour.
     * Uses the golden hour's explicit win probability if set, otherwise multiplies the
     * event location's default win probability by this golden hour's win probability multiplier.
     * 
     * @return the effective win probability during this golden hour
     */
    public Double getWinProbability() {
        double baseProb = winProbability != null ? winProbability :
                (eventLocation != null ? eventLocation.getDefaultWinProbability() : 0.0);
        return baseProb * getWinProbabilityMultiplier();
    }

    /**
     * Checks if this golden hour's status is active and its event location is active.
     * 
     * @return true if both this golden hour and its event location are active, false otherwise
     */
    private boolean isStatusActive() {
        return super.isActive() && eventLocation != null && eventLocation.isActive();
    }

    /**
     * Checks if the specified time is within this golden hour's time window.
     * 
     * @param at the time to check
     * @return true if the time is within the golden hour's start and end times, false otherwise
     */
    private boolean isWithinTimeWindow(LocalDateTime at) {
        if (startTime == null || endTime == null || at == null) {
            return false;
        }
        return !at.isBefore(startTime) && !at.isAfter(endTime);
    }

    /**
     * Checks if this golden hour has not reached its usage limit.
     * 
     * @return true if the golden hour has not reached its daily limit or has no limit, false otherwise
     */
    private boolean isWithinUsageLimit() {
        if (dailyLimit == null) {
            return true;
        }
        return getTotalUses() < dailyLimit;
    }

    /**
     * Determines if this golden hour is active at the specified time.
     * A golden hour is active if its status is active, the time is within its time window,
     * and it has not reached its usage limit.
     * 
     * @param at the time to check
     * @return true if the golden hour is active at the specified time, false otherwise
     */
    public boolean isActive(LocalDateTime at) {
        return isStatusActive() && isWithinTimeWindow(at) && isWithinUsageLimit();
    }

    /**
     * Determines if this golden hour is currently active.
     * 
     * @return true if the golden hour is active at the current time, false otherwise
     */
    public boolean isCurrentlyActive() {
        return isActive(LocalDateTime.now());
    }

    /**
     * Overrides the base isActive method to check if the golden hour is currently active.
     * 
     * @return true if the golden hour is currently active, false otherwise
     */
    @Override
    public boolean isActive() {
        return isCurrentlyActive();
    }

    /**
     * Returns a string representation of this golden hour.
     * 
     * @return a string containing the golden hour's ID, code, location, time window, and probability settings
     */
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
    
    /**
     * Gets the active days of the week for this golden hour as a Set of DayOfWeek.
     * This method is used by mappers to convert the active days to a set representation.
     * 
     * @return a Set containing the days of week when this golden hour is active
     */
    public Set<DayOfWeek> getActiveDaysAsSet() {
        // Create an empty set to store active days
        Set<DayOfWeek> activeDays = new LinkedHashSet<>();
        
        // For now, we'll assume the golden hour is active every day
        // This can be enhanced later to use a specific field that stores active days
        activeDays.add(DayOfWeek.MONDAY);
        activeDays.add(DayOfWeek.TUESDAY);
        activeDays.add(DayOfWeek.WEDNESDAY);
        activeDays.add(DayOfWeek.THURSDAY);
        activeDays.add(DayOfWeek.FRIDAY);
        activeDays.add(DayOfWeek.SATURDAY);
        activeDays.add(DayOfWeek.SUNDAY);
        
        return activeDays;
    }
}