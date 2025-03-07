package vn.com.fecredit.app.entity;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

@Entity
@Table(name = "regions")
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Region extends AbstractStatusAwareEntity {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "name")
    private String name;

    @Setter
    @Column(name = "code", unique = true)
    private String code;

    @Setter
    @Column(name = "description")
    private String description;

    @Setter
    @Column(name = "metadata")
    private String metadata;

    @OneToMany(mappedBy = "region", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Province> provinces = new HashSet<>();

    @OneToMany(mappedBy = "region", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<EventLocation> eventLocations = new HashSet<>();

    public void setProvinces(Set<Province> newProvinces) {
        // Clear old relationships
        provinces.forEach(province -> province.setRegion(null));
        provinces.clear();

        // Set new relationships
        if (newProvinces != null) {
            newProvinces.forEach(this::addProvince);
        }
    }

    public void addProvince(Province province) {
        if (province != null) {
            provinces.add(province);
            if (province.getRegion() != this) {
                province.setRegion(this);
            }
        }
    }

    public void removeProvince(Province province) {
        if (province != null && provinces.contains(province)) {
            provinces.remove(province);
            if (province.getRegion() == this) {
                province.setRegion(null);
            }
        }
    }

    public void setEventLocations(Set<EventLocation> locations) {
        // Clear old relationships
        eventLocations.forEach(location -> location.setRegion(null));
        eventLocations.clear();

        // Set new relationships
        if (locations != null) {
            locations.forEach(location -> {
                eventLocations.add(location);
                if (location.getRegion() != this) {
                    location.setRegion(this);
                }
            });
        }
    }

    @Override
    public String toString() {
        return String.format("Region[id=%d, code=%s, name=%s]",
                id, code, name);
    }

    @Override
    public boolean isActive() {
        return super.isActive();
    }
}
