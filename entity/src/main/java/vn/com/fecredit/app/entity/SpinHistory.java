package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.time.LocalDateTime;

/**
 * Entity representing a record of a participant's spin in the lucky draw system.
 * Each spin history tracks whether the spin resulted in a win, what reward was earned (if any),
 * how many points were earned, and when the spin occurred.
 * <p>
 * Spin histories provide a complete audit trail of all participant activity and are used for
 * analytics, reporting, and ensuring fair play in the lucky draw system.
 */
@Entity
@Table(name = "spin_histories")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SpinHistory extends AbstractStatusAwareEntity {

    /**
     * The participant event record associated with this spin.
     * This establishes the many-to-one relationship with the ParticipantEvent entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_event_id", nullable = false)
    @ToString.Exclude
    private ParticipantEvent participantEvent;

    /**
     * The event location where this spin occurred.
     * This establishes the many-to-one relationship with the EventLocation entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id")
    @ToString.Exclude
    private EventLocation eventLocation;
    
    /**
     * The golden hour during which this spin occurred, if any.
     * This establishes the many-to-one relationship with the GoldenHour entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "golden_hour_id")
    @ToString.Exclude
    private GoldenHour goldenHour;

    /**
     * The reward that was won in this spin, if any.
     * This will be null for losing spins.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    @ToString.Exclude
    private Reward reward;

    /**
     * Flag indicating whether this spin resulted in a win.
     * True if the participant won a reward, false otherwise.
     */
    @Column(name = "win")
    private Boolean win;

    /**
     * The number of points earned from this spin.
     * This will be zero for losing spins.
     */
    @Column(name = "points_earned")
    private Integer pointsEarned;

    /**
     * Flag indicating whether this spin record has been finalized.
     * Once finalized, a spin record cannot be modified.
     */
    @Column(name = "finalized")
    private Boolean finalized;

    /**
     * The date and time when the spin occurred.
     */
    @Column(name = "spin_time")
    private LocalDateTime spinTime;

    /**
     * Additional metadata stored as a JSON string for extensibility.
     */
    @Column(name = "metadata")
    private String metadata;

    /**
     * Initializes default values for new spin history records before they are persisted.
     * Sets the spin time to the current time if not already set.
     * Sets finalized and win flags to false if not already set.
     */
    @PrePersist
    public void prePersist() {
        if (spinTime == null) {
            spinTime = LocalDateTime.now();
        }
        if (finalized == null) {
            finalized = false;
        }
        if (win == null) {
            win = false;
        }
        if (pointsEarned == null) {
            pointsEarned = 0;
        }
    }

    /**
     * Validates the state of this entity before update operations.
     * Ensures that required relationships are established and numeric values are valid.
     * 
     * @throws IllegalStateException if validation fails
     */
    @PreUpdate
    protected void validateState() {
        if (participantEvent == null) {
            throw new IllegalStateException("Participant event is required");
        }
        if (pointsEarned != null && pointsEarned < 0) {
            throw new IllegalStateException("Points earned cannot be negative");
        }
    }

    /**
     * Determines if this spin history record is currently active.
     * A spin history is active if its base status is active and its associated participant event is active.
     * 
     * @return true if the spin history is active, false otherwise
     */
    @Override
    public boolean isActive() {
        return super.isActive() && 
               participantEvent != null && 
               participantEvent.isActive();
    }

    /**
     * Checks if this spin resulted in a win.
     * Handles null values safely.
     * 
     * @return true if this was a winning spin, false otherwise
     */
    public boolean isWin() {
        return Boolean.TRUE.equals(win);
    }

    /**
     * Checks if this spin history record has been finalized.
     * Finalized records cannot be modified.
     * Handles null values safely.
     * 
     * @return true if the record is finalized, false otherwise
     */
    public boolean isFinalized() {
        return Boolean.TRUE.equals(finalized);
    }

    /**
     * Marks this spin as a win, associating it with a reward and updating the points earned.
     * 
     * @param reward the reward that was won
     * @param points the number of points earned from this win
     */
    public void markAsWin(Reward reward, Integer points) {
        this.reward = reward;
        this.win = true;
        this.pointsEarned = points != null ? points : 0;
    }

    /**
     * Marks this spin as a loss, clearing any reward and setting points earned to zero.
     */
    public void markAsLoss() {
        this.reward = null;
        this.win = false;
        this.pointsEarned = 0;
    }

    /**
     * Finalizes this spin history record, preventing further modifications.
     * 
     * @throws IllegalStateException if the record is already finalized
     */
    public void finalize() {
        if (isFinalized()) {
            throw new IllegalStateException("Spin history is already finalized");
        }
        this.finalized = true;
    }
}
