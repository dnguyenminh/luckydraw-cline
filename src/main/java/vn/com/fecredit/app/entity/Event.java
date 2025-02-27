package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "events")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Event extends AbstractStatusAwareEntity implements Versionable {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Integer totalSpins;

    @Column(nullable = false)
    private Integer remainingSpins;

    @Column(nullable = false)
    private Integer dailySpinLimit;

    @Version
    private Long version;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventLocation> locations = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Reward> rewards = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GoldenHour> goldenHours = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Participant> participants = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SpinHistory> spinHistories = new HashSet<>();

    @PrePersist
    @Override
    public void prePersist() {
        super.prePersist();
        if (this.remainingSpins == null) {
            this.remainingSpins = this.totalSpins;
        }
    }

    public boolean isActive(LocalDateTime currentTime) {
        return isActive() && 
               currentTime.isAfter(startTime) && 
               currentTime.isBefore(endTime);
    }

    public boolean hasAvailableSpins() {
        return remainingSpins > 0;
    }

    public boolean decrementRemainingSpins() {
        if (remainingSpins > 0) {
            remainingSpins--;
            return true;
        }
        return false;
    }

    public void addLocation(EventLocation location) {
        locations.add(location);
        location.setEvent(this);
    }

    public void removeLocation(EventLocation location) {
        locations.remove(location);
        location.setEvent(null);
    }

    public void addReward(Reward reward) {
        rewards.add(reward);
        reward.setEvent(this);
    }

    public void removeReward(Reward reward) {
        rewards.remove(reward);
        reward.setEvent(null);
    }

    public void addGoldenHour(GoldenHour goldenHour) {
        goldenHours.add(goldenHour);
        goldenHour.setEvent(this);
    }

    public void removeGoldenHour(GoldenHour goldenHour) {
        goldenHours.remove(goldenHour);
        goldenHour.setEvent(null);
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
        participant.setEvent(this);
    }

    public void removeParticipant(Participant participant) {
        participants.remove(participant);
        participant.setEvent(null);
    }

    public void addSpinHistory(SpinHistory spinHistory) {
        spinHistories.add(spinHistory);
        spinHistory.setEvent(this);
    }

    public void removeSpinHistory(SpinHistory spinHistory) {
        spinHistories.remove(spinHistory);
        spinHistory.setEvent(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event event)) return false;
        return getId() != null && getId().equals(event.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
