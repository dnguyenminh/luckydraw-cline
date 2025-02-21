package vn.com.fecredit.app.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "total_spins", nullable = false)
    @Builder.Default
    private Long totalSpins = 0L;

    @Column(name = "remaining_spins", nullable = false)
    @Builder.Default
    private Long remainingSpins = 0L;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;

    @OneToMany(mappedBy = "event")
    @Builder.Default
    private Set<EventLocation> eventLocations = new HashSet<>();

    @OneToMany(mappedBy = "event")
    @Builder.Default
    private Set<Reward> rewards = new HashSet<>();

    @OneToMany(mappedBy = "event")
    @Builder.Default
    private Set<Participant> participants = new HashSet<>();

    @OneToMany(mappedBy = "event")
    @Builder.Default
    private Set<SpinHistory> spinHistories = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void addLocation(EventLocation location) {
        eventLocations.add(location);
        location.setEvent(this);
    }

    public void removeLocation(EventLocation location) {
        eventLocations.remove(location);
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

    public synchronized void decrementRemainingSpins() {
        if (remainingSpins > 0) {
            remainingSpins--;
        }
    }

    public boolean isInProgress() {
        LocalDateTime now = LocalDateTime.now();
        return isActive() && 
               (startDate == null || startDate.isBefore(now)) &&
               (endDate == null || endDate.isAfter(now));
    }

    public boolean hasSpinsAvailable() {
        return isActive() && remainingSpins > 0;
    }
}