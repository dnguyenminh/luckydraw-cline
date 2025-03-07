package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

@Entity
@Table(name = "events")
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Event extends AbstractStatusAwareEntity {

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
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Setter
    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Setter
    @Column(name = "initial_spins")
    @Builder.Default
    private Integer initialSpins = 10;

    @Setter
    @Column(name = "daily_spin_limit")
    @Builder.Default
    private Integer dailySpinLimit = 5;

    @Setter
    @Column(name = "default_win_probability")
    @Builder.Default
    private Double defaultWinProbability = 0.1;

    @Setter
    @Column(name = "metadata")
    private String metadata;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<EventLocation> eventLocations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Reward> rewards = new LinkedHashSet<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<GoldenHour> goldenHours = new LinkedHashSet<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<SpinHistory> spinHistories = new LinkedHashSet<>();

    public Set<EventLocation> getLocations() {
        return eventLocations;
    }

    public Set<ParticipantEvent> getParticipants() {
        return eventLocations.stream()
            .flatMap(location -> location.getParticipantEvents().stream())
            .collect(Collectors.toSet());
    }

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

    public void removeLocation(EventLocation location) {
        if (location != null && eventLocations.contains(location)) {
            eventLocations.remove(location);
            if (location.getEvent() == this) {
                location.setEvent(null);
            }
        }
    }

    public EventLocation getDefaultLocation() {
        return eventLocations.isEmpty() ? null : eventLocations.iterator().next();
    }

    public boolean hasOverlappingProvinces(EventLocation newLocation) {
        if (newLocation == null || newLocation.getRegion() == null) {
            return false;
        }

        Set<Province> newLocationProvinces = newLocation.getRegion().getProvinces();
        Set<Province> existingProvinces = eventLocations.stream()
            .map(EventLocation::getRegion)
            .filter(region -> region != null)
            .flatMap(region -> region.getProvinces().stream())
            .collect(Collectors.toSet());

        return newLocationProvinces.stream().anyMatch(existingProvinces::contains);
    }

    @Override
    public boolean isActive() {
        boolean baseActive = super.isActive();
        if (!baseActive) {
            System.out.println("Event base status check failed");
            return false;
        }

        if (startTime == null || endTime == null) {
            System.out.println("Event time range null check failed");
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean timeValid = (now.isEqual(startTime) || now.isAfter(startTime)) && 
                          (now.isEqual(endTime) || now.isBefore(endTime));
        
        if (!timeValid) {
            System.out.println("Event time range check failed");
            System.out.println("now: " + now);
            System.out.println("startTime: " + startTime);
            System.out.println("endTime: " + endTime);
        }

        return timeValid;
    }

    @Override
    public String toString() {
        return String.format("Event[id=%d, code=%s, name=%s]",
                id, code, name);
    }
}
