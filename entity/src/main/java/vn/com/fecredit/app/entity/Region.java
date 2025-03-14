package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.ArrayList;
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
@Table(name = "regions", schema = "public")
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Region extends AbstractStatusAwareEntity {

    /**
     * The name of the region displayed to users.
     */
    @ToString.Include
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Unique code identifier for the region, used in APIs and references.
     */
    @ToString.Include
    @Column(nullable = false, length = 20, unique = true)
    private String code;

    /**
     * Sets the region code and normalizes it to uppercase
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code != null ? code.toUpperCase() : null;
    }

    /**
     * Lifecycle callback to normalize code before persisting or updating
     */
    @PrePersist
    @PreUpdate
    protected void normalizeCode() {
        if (code != null) {
            code = code.toUpperCase();
        }
    }

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
    private List<Province> provinces = new ArrayList<>();

    /**
     * The collection of event locations situated within this region.
     * This establishes the one-to-many relationship with the EventLocation entity.
     */
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<EventLocation> eventLocations = new ArrayList<>();

    public void addProvince(Province province) {
        provinces.add(province);
        province.setRegion(this);
    }

    public void removeProvince(Province province) {
        provinces.remove(province);
        province.setRegion(null);
    }

    public void addEventLocation(EventLocation location) {
        eventLocations.add(location);
        location.setRegion(this);
    }

    public void removeEventLocation(EventLocation location) {
        eventLocations.remove(location);
        location.setRegion(null);
    }

    public int getProvinceCount() {
        return provinces.size();
    }

    public int getLocationCount() {
        return eventLocations.size();
    }
    
    public int getActiveProvinceCount() {
        return (int) provinces.stream()
            .filter(Province::isActive)
            .count();
    }

    public int getActiveLocationCount() {
        return (int) eventLocations.stream()
            .filter(EventLocation::isActive)
            .count();
    }

    public double getProvinceActivationRate() {
        if (provinces.isEmpty()) {
            return 0.0;
        }
        return getActiveProvinceCount() / (double) provinces.size();
    }

    public double getLocationActivationRate() {
        if (eventLocations.isEmpty()) {
            return 0.0;
        }
        return getActiveLocationCount() / (double) eventLocations.size();
    }

    @Override
    protected void onActivate() {
        // No specific activation logic needed
    }

    @Override
    protected void onDeactivate() {
        if (hasActiveProvinces()) {
            throw new IllegalStateException("Cannot deactivate region with active provinces");
        }
        if (hasActiveEventLocations()) {
            throw new IllegalStateException("Cannot deactivate region with active event locations");
        }
    }

    public boolean hasActiveProvinces() {
        return provinces.stream().anyMatch(Province::isActive);
    }

    public boolean hasActiveEventLocations() {
        return eventLocations.stream().anyMatch(EventLocation::isActive);
    }

    public boolean hasProvinces() {
        return !provinces.isEmpty();
    }

    public boolean hasEventLocations() {
        return !eventLocations.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Region)) return false;
        Region region = (Region) o;
        return code != null && code.equals(region.getCode());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
