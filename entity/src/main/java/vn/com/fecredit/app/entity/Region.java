package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "region")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Region extends AbstractStatusAwareEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "default_win_probability")
    private Double defaultWinProbability;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Province> provinces = new ArrayList<>();

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

    @PrePersist
    @PreUpdate
    protected void validateState() {
        if (code != null) {
            code = code.toUpperCase();
        }
    }

    // Adapter methods to support legacy code expecting Sets
    public List<Province> getProvinces() {
        return provinces;
    }

    public List<EventLocation> getEventLocations() {
        return eventLocations;
    }

    public void setProvinces(List<Province> provinces) {
        this.provinces.clear();
        if (provinces != null) {
            this.provinces.addAll(provinces);
        }
    }

    public void setEventLocations(List<EventLocation> locations) {
        this.eventLocations.clear();
        if (locations != null) {
            this.eventLocations.addAll(locations);
        }
    }
}
