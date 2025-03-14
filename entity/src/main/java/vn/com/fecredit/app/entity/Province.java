package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "provinces")
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Province extends AbstractStatusAwareEntity {

    @ToString.Include
    @Column(nullable = false, length = 100)
    private String name;

    @ToString.Include
    @Column(nullable = false, length = 20)
    private String code;

    @Column(name = "population")
    private Long population;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "metadata")
    private String metadata;
    
    @Column(name = "default_win_probability")
    private Double defaultWinProbability;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @ToString.Exclude
    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Participant> participants = new ArrayList<>();

    @ToString.Exclude
    @ManyToMany(mappedBy = "provinces", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Event> events = new HashSet<>();

    @PrePersist
    @PreUpdate
    protected void validateState() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Province name is required");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalStateException("Province code is required");
        }
        if (region == null) {
            throw new IllegalStateException("Region is required");
        }
        code = code.toUpperCase();
    }

    @Override
    protected void onActivate() {
        if (region == null || !region.isActive()) {
            throw new IllegalStateException("Cannot activate province with inactive region");
        }
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
        participant.setProvince(this);
    }

    public void removeParticipant(Participant participant) {
        participants.remove(participant);
        participant.setProvince(null);
    }

    public void addEvent(Event event) {
        events.add(event);
        event.getProvinces().add(this);
    }

    public void removeEvent(Event event) {
        events.remove(event);
        event.getProvinces().remove(this);
    }
    
    /**
     * Gets the effective default win probability for this province.
     * If the province has its own default win probability set, that value is used.
     * Otherwise, it falls back to the region's default win probability.
     * If neither is set, returns 0.0 as a default value.
     *
     * @return the effective default win probability
     */
    public Double getEffectiveDefaultWinProbability() {
        if (defaultWinProbability != null) {
            return defaultWinProbability;
        }
        
        if (region != null && region.getDefaultWinProbability() != null) {
            return region.getDefaultWinProbability();
        }
        
        return 0.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Province)) return false;
        Province province = (Province) o;
        return code != null && code.equals(province.getCode());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
