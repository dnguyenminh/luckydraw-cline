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
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Province extends AbstractStatusAwareEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(name = "population")
    private Long population;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "metadata")
    private String metadata;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @ToString.Exclude
    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Participant> participants = new ArrayList<>();

    @ToString.Exclude
    @ManyToMany(mappedBy = "provinces")
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

    @Override
    public String toString() {
        return String.format("Province[id=%d, code=%s, name=%s]",
                getId(), code, name);
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
