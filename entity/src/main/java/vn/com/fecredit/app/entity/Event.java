package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

/**
 * Entity representing an event in the lucky draw system.
 * An event is a time-bounded activity during which participants can spin for rewards.
 * Each event can have multiple locations and defines the basic parameters for the lucky draw,
 * such as spin limits and default win probability.
 */
@Entity
@Table(name = "events", schema = "public")
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Event extends AbstractStatusAwareEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for the event.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the event displayed to users.
     */
    @Setter
    @Column(name = "name")
    private String name;

    /**
     * Unique code identifier for the event, used in APIs and references.
     */
    @Setter
    @Column(name = "code", unique = true)
    private String code;

    /**
     * Detailed description of the event.
     */
    @Setter
    @Column(name = "description")
    private String description;

    /**
     * The date and time when the event starts.
     * The event is only active after this time.
     */
    @Setter
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * The date and time when the event ends.
     * The event is inactive after this time.
     */
    @Setter
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * The initial number of spins allocated to participants when they join the event.
     * Default is 10 spins.
     */
    @Setter
    @Column(name = "initial_spins")
    @Builder.Default
    private Integer initialSpins = 10;

    /**
     * The maximum number of spins a participant can use per day.
     * Default is 5 spins per day.
     */
    @Setter
    @Column(name = "daily_spin_limit")
    @Builder.Default
    private Integer dailySpinLimit = 5;

    /**
     * The default probability of winning a reward when spinning.
     * This can be overridden at the location, golden hour, or reward level.
     * Default is 0.1 (10% chance).
     */
    @Setter
    @Column(name = "default_win_probability")
    @Builder.Default
    private Double defaultWinProbability = 0.1;

    /**
     * Additional metadata stored as a JSON string for extensibility.
     */
    @Setter
    @Column(name = "metadata")
    private String metadata;

    /**
     * The set of provinces associated with this event.
     * Managed through the event_provinces join table.
     */
    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "event_provinces",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "province_id")
    )
    @Builder.Default
    private Set<Province> provinces = new LinkedHashSet<>();

    /**
     * The collection of event locations associated with this event.
     * This establishes a one-to-many relationship with EventLocation entity.
     */
    @ToString.Exclude
    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<EventLocation> eventLocations = new LinkedHashSet<>();

    /**
     * Gets all locations associated with this event.
     * This is an alias for eventLocations for backward compatibility.
     * 
     * @return the set of event locations
     */
    public Set<EventLocation> getLocations() {
        return eventLocations;
    }

    /**
     * Adds a location to this event.
     * Validates that the location's provinces don't overlap with existing locations.
     * Establishes the bidirectional relationship between event and location.
     * 
     * @param location the location to add
     * @throws IllegalArgumentException if the location's provinces overlap with existing locations
     */
    public void addLocation(EventLocation location) {
        if (location == null) return;

        if (hasOverlappingProvinces(location)) {
            throw new IllegalArgumentException("Event already has a location covering some of these provinces");
        }

        eventLocations.add(location);
        if (location.getEvent() != this) {
            location.setEvent(this);
        }
    }

    /**
     * Removes a location from this event.
     * Breaks the bidirectional relationship between event and location.
     * 
     * @param location the location to remove
     */
    public void removeLocation(EventLocation location) {
        if (location != null && eventLocations.contains(location)) {
            eventLocations.remove(location);
            if (location.getEvent() == this) {
                location.setEvent(null);
            }
        }
    }

    /**
     * Gets the default location for this event.
     * This is the first location in the set, or null if there are no locations.
     * 
     * @return the default location, or null if none exists
     */
    public EventLocation getDefaultLocation() {
        return eventLocations.isEmpty() ? null : eventLocations.iterator().next();
    }

    /**
     * Checks if the provinces of a new location overlap with the provinces of existing locations.
     * 
     * @param newLocation the new location to check
     * @return true if there is an overlap, false otherwise
     */
    public boolean hasOverlappingProvinces(EventLocation newLocation) {
        if (newLocation == null || newLocation.getRegion() == null) {
            return false;
        }

        List<Province> newLocationProvinces = newLocation.getRegion().getProvinces();
        Set<Province> existingProvinces = eventLocations.stream()
            .map(EventLocation::getRegion)
            .filter(region -> region != null)
            .flatMap(region -> region.getProvinces().stream())
            .collect(Collectors.toSet());

        return newLocationProvinces.stream().anyMatch(existingProvinces::contains);
    }

    /**
     * Determines if this event is currently active.
     * An event is active if its base status is active and the current time is within the event's time range.
     * 
     * @return true if the event is active, false otherwise
     */
    @Override
    public boolean isActive() {
        boolean baseActive = super.isActive();
        if (!baseActive) return false;

        if (startTime != null && endTime != null) {
            LocalDateTime now = LocalDateTime.now();
            return (now.isEqual(startTime) || now.isAfter(startTime)) && 
                   (now.isEqual(endTime) || now.isBefore(endTime));
        }
        return false;
    }

    /**
     * Returns a string representation of this event.
     * 
     * @return a string containing the event's ID, code, and name
     */
    @Override
    public String toString() {
        return String.format("Event[id=%d, code=%s, name=%s]",
                id, code, name);
    }

    /**
     * The total number of spins remaining for this event.
     */
    @Setter
    @Column(name = "remaining_spins")
    private Long remainingSpins;

    /**
     * Adds a province to this event.
     * Establishes the bidirectional relationship between event and province.
     * 
     * @param province the province to add
     */
    public void addProvince(Province province) {
        if (province != null) {
            provinces.add(province);
            province.getEvents().add(this);
        }
    }

    /**
     * Removes a province from this event.
     * Breaks the bidirectional relationship between event and province.
     * 
     * @param province the province to remove
     */
    public void removeProvince(Province province) {
        if (province != null) {
            provinces.remove(province);
            province.getEvents().remove(this);
        }
    }
}
