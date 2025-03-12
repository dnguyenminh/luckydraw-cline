package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;

/**
 * Entity representing a geographical region in the lucky draw system.
 * Regions contain provinces and can host event locations.
 * <p>
 * Regions provide a way to organize the geographical scope of events and manage
 * default win probabilities for specific areas. They also enforce hierarchical
 * activation/deactivation rules with their contained provinces and event locations.
 */
@Entity
@Table(name = "region")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Region extends AbstractStatusAwareEntity {

    /**
     * The name of the region displayed to users.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Unique code identifier for the region, used in APIs and references.
     */
    @Column(nullable = false, length = 20, unique = true)
    private String code;

    /**
     * The default probability of winning a reward when spinning in this region.
     * This can be overridden at the event location, golden hour, or reward level.
     */
    @Column(name = "default_win_probability")
    private Double defaultWinProbability;

    /**
     * The collection of provinces contained within this region.
     * This establishes the one-to-many relationship with the Province entity.
     */
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private Set<Province> provinces = new LinkedHashSet<>();

    /**
     * The collection of event locations situated within this region.
     * This establishes the one-to-many relationship with the EventLocation entity.
     */
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private Set<EventLocation> eventLocations = new LinkedHashSet<>();

    /**
     * Adds a province to this region.
     * Establishes the bidirectional relationship between region and province.
     * 
     * @param province the province to add
     */
    public void addProvince(Province province) {
        provinces.add(province);
        province.setRegion(this);
    }

    /**
     * Removes a province from this region.
     * Breaks the bidirectional relationship between region and province.
     * 
     * @param province the province to remove
     */
    public void removeProvince(Province province) {
        provinces.remove(province);
        province.setRegion(null);
    }

    /**
     * Adds an event location to this region.
     * Establishes the bidirectional relationship between region and event location.
     * 
     * @param location the event location to add
     */
    public void addEventLocation(EventLocation location) {
        eventLocations.add(location);
        location.setRegion(this);
    }

    /**
     * Removes an event location from this region.
     * Breaks the bidirectional relationship between region and event location.
     * 
     * @param location the event location to remove
     */
    public void removeEventLocation(EventLocation location) {
        eventLocations.remove(location);
        location.setRegion(null);
    }

    /**
     * Gets the total number of provinces in this region.
     * 
     * @return the count of provinces
     */
    public int getProvinceCount() {
        return provinces.size();
    }

    /**
     * Gets the total number of event locations in this region.
     * 
     * @return the count of event locations
     */
    public int getLocationCount() {
        return eventLocations.size();
    }
    
    /**
     * Gets the number of active provinces in this region.
     * 
     * @return the count of active provinces
     */
    public int getActiveProvinceCount() {
        return (int) provinces.stream()
            .filter(Province::isActive)
            .count();
    }

    /**
     * Gets the number of active event locations in this region.
     * 
     * @return the count of active event locations
     */
    public int getActiveLocationCount() {
        return (int) eventLocations.stream()
            .filter(EventLocation::isActive)
            .count();
    }

    /**
     * Calculates the percentage of provinces in this region that are active.
     * 
     * @return the activation rate as a decimal between 0.0 and 1.0, or 0.0 if no provinces exist
     */
    public double getProvinceActivationRate() {
        if (provinces.isEmpty()) {
            return 0.0;
        }
        return getActiveProvinceCount() / (double) provinces.size();
    }

    /**
     * Calculates the percentage of event locations in this region that are active.
     * 
     * @return the activation rate as a decimal between 0.0 and 1.0, or 0.0 if no locations exist
     */
    public double getLocationActivationRate() {
        if (eventLocations.isEmpty()) {
            return 0.0;
        }
        return getActiveLocationCount() / (double) eventLocations.size();
    }

    /**
     * Custom logic executed when this entity is activated.
     * No specific activation logic is needed for regions.
     */
    @Override
    protected void onActivate() {
        // No specific activation logic needed
    }

    /**
     * Custom logic executed when this entity is deactivated.
     * Prevents deactivation if there are any active provinces or event locations in this region.
     * 
     * @throws IllegalStateException if there are active provinces or event locations
     */
    @Override
    protected void onDeactivate() {
        if (hasActiveProvinces()) {
            throw new IllegalStateException("Cannot deactivate region with active provinces");
        }
        if (hasActiveEventLocations()) {
            throw new IllegalStateException("Cannot deactivate region with active event locations");
        }
    }

    /**
     * Checks if this region has any active provinces.
     * 
     * @return true if there is at least one active province, false otherwise
     */
    public boolean hasActiveProvinces() {
        return provinces.stream().anyMatch(Province::isActive);
    }

    /**
     * Checks if this region has any active event locations.
     * 
     * @return true if there is at least one active event location, false otherwise
     */
    public boolean hasActiveEventLocations() {
        return eventLocations.stream().anyMatch(EventLocation::isActive);
    }

    /**
     * Validates the state of this entity before persistence or update operations.
     * Ensures that the code is uppercase.
     */
    @PrePersist
    @PreUpdate
    protected void validateState() {
        if (code != null) {
            code = code.toUpperCase();
        }
    }

    /**
     * Gets the provinces in this region.
     * This is an adapter method to support legacy code expecting Sets.
     * 
     * @return the set of provinces
     */
    public Set<Province> getProvinces() {
        return provinces;
    }

    /**
     * Gets the event locations in this region.
     * This is an adapter method to support legacy code expecting Sets.
     * 
     * @return the set of event locations
     */
    public Set<EventLocation> getEventLocations() {
        return eventLocations;
    }

    /**
     * Sets the provinces in this region from a list.
     * This is an adapter method to support legacy code using Lists.
     * 
     * @param provinces the list of provinces to set
     */
    public void setProvinces(List<Province> provinces) {
        this.provinces.clear();
        if (provinces != null) {
            this.provinces.addAll(provinces);
        }
    }

    /**
     * Sets the event locations in this region from a list.
     * This is an adapter method to support legacy code using Lists.
     * 
     * @param locations the list of event locations to set
     */
    public void setEventLocations(List<EventLocation> locations) {
        this.eventLocations.clear();
        if (locations != null) {
            this.eventLocations.addAll(locations);
        }
    }
}
