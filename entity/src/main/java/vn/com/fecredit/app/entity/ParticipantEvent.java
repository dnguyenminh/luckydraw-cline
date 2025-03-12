package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing the relationship between a Participant and an Event at a specific EventLocation.
 * This entity tracks a participant's engagement with an event, including their spin history,
 * remaining spins, daily usage, and rewards earned.
 * <p>
 * It serves as the central record for all participant activity within a specific event location,
 * managing spin allocations, usage tracking, and win statistics.
 */
@Entity
@Table(name = "participant_events")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantEvent extends AbstractStatusAwareEntity {

    /**
     * The participant associated with this event participation record.
     * This establishes the many-to-one relationship with the Participant entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    @ToString.Exclude
    private Participant participant;

    /**
     * The event that the participant is engaged with.
     * This establishes the many-to-one relationship with the Event entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private Event event;

    /**
     * The specific location of the event where the participant is participating.
     * This establishes the many-to-one relationship with the EventLocation entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id", nullable = false)
    @ToString.Exclude
    private EventLocation eventLocation;
    
    /**
     * The province associated with this participant event.
     * This establishes the many-to-one relationship with the Province entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    @ToString.Exclude
    private Province province;

    /**
     * The total number of spins allocated to the participant for this event.
     * This includes both used and remaining spins.
     */
    @Column(name = "total_spins")
    @Builder.Default
    private Integer totalSpins = 0;

    /**
     * The number of spins the participant has left to use in this event.
     * This decreases as the participant uses spins.
     */
    @Column(name = "remaining_spins")
    @Builder.Default
    private Integer remainingSpins = 0;

    /**
     * The initial number of spins allocated to the participant when they joined the event.
     * This value doesn't change over time and serves as a reference point.
     */
    @Column(name = "initial_spins")
    @Builder.Default
    private Integer initialSpins = 0;

    /**
     * The number of spins used by the participant on the current day.
     * This is reset daily and used to enforce daily spin limits.
     */
    @Column(name = "daily_spins_used")
    @Builder.Default
    private Integer dailySpinsUsed = 0;

    /**
     * The total number of winning spins the participant has had in this event.
     */
    @Column(name = "total_wins")
    @Builder.Default
    private Integer totalWins = 0;
    
    /**
     * The total points accumulated by the participant in this event.
     */
    @Column(name = "total_points")
    @Builder.Default
    private Integer totalPoints = 0;
    
    /**
     * Additional metadata stored as a JSON string for extensibility.
     */
    @Column(name = "metadata")
    private String metadata;

    /**
     * The collection of spin history records associated with this participant's event participation.
     * This establishes the one-to-many relationship with the SpinHistory entity.
     */
    @OneToMany(mappedBy = "participantEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<SpinHistory> spinHistories = new LinkedHashSet<>();

    /**
     * Gets the number of spins available to the participant.
     * @return The number of remaining spins
     */
    public Integer getAvailableSpins() {
        return remainingSpins != null ? remainingSpins : 0;
    }

    /**
     * Gets the timestamp of the participant's last spin.
     * @return The timestamp of the last spin, or null if no spins have been made
     */
    public LocalDateTime getLastSpinTime() {
        return spinHistories.stream()
            .map(SpinHistory::getSpinTime)
            .max(LocalDateTime::compareTo)
            .orElse(null);
    }

    /**
     * Gets the timestamp of the last synchronization.
     * @return The timestamp of the last sync, or null if no sync has occurred
     */
    public LocalDateTime getLastSyncTime() {
        return getUpdatedAt();
    }

    /**
     * Validates the state of this entity before persistence or update operations.
     * Ensures that required relationships are established and numeric values are valid.
     * 
     * @throws IllegalStateException if validation fails
     */
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

    /**
     * Custom logic executed when this entity is activated.
     * Ensures that related entities (event, location, participant) are also active.
     * 
     * @throws IllegalStateException if any related entity is inactive
     */
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
    
    /**
     * Method for testing the onActivate logic.
     * This allows unit tests to verify activation rules without changing entity state.
     */
    public void testOnActivate() {
        onActivate();
    }

    /**
     * Custom logic executed when this entity is deactivated.
     * Prevents deactivation if there are any pending (non-finalized) spins.
     * 
     * @throws IllegalStateException if there are pending spins
     */
    @Override
    protected void onDeactivate() {
        if (!spinHistories.isEmpty() && 
            spinHistories.stream().anyMatch(sh -> !sh.isFinalized())) {
            throw new IllegalStateException("Cannot deactivate participant event with pending spins");
        }
    }

    /**
     * Adds a spin history record to this participant event.
     * Updates the daily spins used count and decrements remaining spins.
     * 
     * @param spinHistory the spin history record to add
     */
    public void addSpinHistory(SpinHistory spinHistory) {
        spinHistories.add(spinHistory);
        spinHistory.setParticipantEvent(this);
        dailySpinsUsed++;
        remainingSpins--;
    }

    /**
     * Removes a spin history record from this participant event.
     * Updates the daily spins used count and increments remaining spins.
     * 
     * @param spinHistory the spin history record to remove
     */
    public void removeSpinHistory(SpinHistory spinHistory) {
        if (spinHistories.remove(spinHistory)) {
            spinHistory.setParticipantEvent(null);
            dailySpinsUsed--;
            remainingSpins++;
        }
    }

    /**
     * Checks if this participant has any remaining spins available.
     * 
     * @return true if there are remaining spins, false otherwise
     */
    public boolean hasRemainingSpins() {
        return remainingSpins > 0;
    }

    /**
     * Checks if this participant has reached their daily spin limit.
     * 
     * @return true if the daily limit has been reached, false otherwise
     */
    public boolean hasReachedDailyLimit() {
        return dailySpinsUsed >= eventLocation.getEffectiveDailySpinLimit();
    }

    /**
     * Resets the daily spins used counter to zero.
     * This is typically called at the start of a new day.
     */
    public void resetDailySpins() {
        dailySpinsUsed = 0;
    }

    /**
     * Calculates the win rate for this participant in this event.
     * The win rate is the ratio of winning spins to total spins.
     * 
     * @return the win rate as a decimal between 0 and 1
     */
    public double getWinRate() {
        if (spinHistories.isEmpty()) {
            return 0.0;
        }
        
        long winCount = spinHistories.stream()
            .filter(SpinHistory::isWin)
            .count();
            
        return (double) winCount / spinHistories.size();
    }

    /**
     * Gets the total number of spins used by this participant in this event.
     * 
     * @return the total number of spins used
     */
    public int getSpinsUsed() {
        return totalSpins - remainingSpins;
    }

    /**
     * Adds points to this participant's total for this event.
     * 
     * @param points the number of points to add
     */
    public void addPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative");
        }
        totalPoints += points;
    }

    /**
     * Increments the total wins counter for this participant in this event.
     */
    public void incrementWins() {
        totalWins++;
    }
    
    /**
     * Increments the spin count for this participant in this event.
     * This is called when a participant uses a spin.
     */
    public void incrementSpinCount() {
        // The spin count is tracked through the spinHistories collection
        // and the dailySpinsUsed counter, which are updated in addSpinHistory
    }
    
    /**
     * Increments the win count for this participant in this event.
     * This is an alias for incrementWins() for API consistency.
     */
    public void incrementWinCount() {
        incrementWins();
    }
    
    /**
     * Resets the daily spin count for this participant in this event.
     * This is an alias for resetDailySpins() for API consistency.
     */
    public void resetDailySpinCount() {
        resetDailySpins();
    }
    
    /**
     * Gets the daily spin count for this participant in this event.
     * 
     * @return the number of spins used today
     */
    public int getDailySpinCount() {
        return dailySpinsUsed;
    }
    
    /**
     * Checks if this participant can spin in this event.
     * A participant can spin if they have remaining spins and haven't reached their daily limit.
     * 
     * @return true if the participant can spin, false otherwise
     */
    public boolean canSpin() {
        return hasRemainingSpins() && !hasReachedDailyLimit();
    }
}
