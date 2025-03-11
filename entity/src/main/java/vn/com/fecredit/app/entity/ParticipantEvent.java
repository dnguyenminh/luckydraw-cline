package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "participant_events")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantEvent extends AbstractStatusAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    @ToString.Exclude
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id", nullable = false)
    @ToString.Exclude
    private EventLocation eventLocation;

    @Column(name = "total_spins")
    private Integer totalSpins = 0;

    @Column(name = "remaining_spins")
    private Integer remainingSpins = 0;

    @Column(name = "initial_spins")
    private Integer initialSpins = 0;

    @Column(name = "daily_spins_used")
    private Integer dailySpinsUsed = 0;

    @Column(name = "metadata")
    private String metadata;

    @OneToMany(mappedBy = "participantEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<SpinHistory> spinHistories = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void validateState() {
        if (event == null) {
            throw new IllegalStateException("Event is required");
        }
        if (eventLocation == null) {
            throw new IllegalStateException("Event location is required");
        }
        if (participant == null) {
            throw new IllegalStateException("Participant is required");
        }
        if (totalSpins != null && totalSpins < 0) {
            throw new IllegalStateException("Total spins cannot be negative");
        }
        if (remainingSpins != null && remainingSpins < 0) {
            throw new IllegalStateException("Remaining spins cannot be negative");
        }
        if (dailySpinsUsed != null && dailySpinsUsed < 0) {
            throw new IllegalStateException("Daily spins used cannot be negative");
        }
    }

    @Override
    protected void onActivate() {
        if (event == null || !event.isActive()) {
            throw new IllegalStateException("Cannot activate participant event for inactive event");
        }
        if (eventLocation == null || !eventLocation.isActive()) {
            throw new IllegalStateException("Cannot activate participant event for inactive location");
        }
        if (participant == null || !participant.isActive()) {
            throw new IllegalStateException("Cannot activate participant event for inactive participant");
        }
    }

    @Override
    protected void onDeactivate() {
        if (!spinHistories.isEmpty() && 
            spinHistories.stream().anyMatch(sh -> !sh.isFinalized())) {
            throw new IllegalStateException("Cannot deactivate participant event with pending spins");
        }
    }

    public void addSpinHistory(SpinHistory spinHistory) {
        spinHistories.add(spinHistory);
        spinHistory.setParticipantEvent(this);
        dailySpinsUsed++;
        remainingSpins--;
    }

    public void removeSpinHistory(SpinHistory spinHistory) {
        if (spinHistories.remove(spinHistory)) {
            spinHistory.setParticipantEvent(null);
            if (dailySpinsUsed > 0) {
                dailySpinsUsed--;
            }
            if (remainingSpins < totalSpins) {
                remainingSpins++;
            }
        }
    }

    public boolean hasRemainingSpins() {
        return remainingSpins > 0;
    }

    public boolean hasReachedDailyLimit() {
        return dailySpinsUsed >= 10; // Default daily limit
    }

    public void resetDailySpins() {
        dailySpinsUsed = 0;
    }

    public boolean canSpin() {
        return isActive() && hasRemainingSpins() && !hasReachedDailyLimit();
    }

    public int getWinningSpinsCount() {
        return (int) spinHistories.stream()
                .filter(SpinHistory::isWin)
                .count();
    }

    public double getWinRate() {
        return spinHistories.isEmpty() ? 0.0 : 
            (double) getWinningSpinsCount() / spinHistories.size();
    }

    public boolean isActive() {
        return getStatus() == 1;
    }
}
