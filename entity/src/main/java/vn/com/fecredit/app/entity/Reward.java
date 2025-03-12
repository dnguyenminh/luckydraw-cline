package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

/**
 * Entity representing a reward that can be won by participants in the lucky draw system.
 * Rewards are associated with specific event locations and have various properties such as
 * point values, quantities, time validity, and win probabilities.
 * <p>
 * The reward system includes inventory management (total and remaining quantities),
 * daily limits, point requirements, and validity periods to control when and how often
 * rewards can be distributed.
 */
@Entity
@Table(name = "rewards")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Reward extends AbstractStatusAwareEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for the reward.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The event location where this reward is available.
     * This establishes the many-to-one relationship with the EventLocation entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id")
    private EventLocation eventLocation;

    /**
     * The name of the reward displayed to users.
     */
    @Column(name = "name")
    private String name;

    /**
     * Unique code identifier for the reward, used in APIs and references.
     */
    @Column(name = "code", unique = true)
    private String code;

    /**
     * Detailed description of the reward.
     */
    @Column(name = "description")
    private String description;

    /**
     * The number of points awarded to a participant when they win this reward.
     */
    @Column(name = "points")
    private Integer points;

    /**
     * The number of points a participant must have to be eligible for this reward.
     * Used for redemption-based rewards rather than random win rewards.
     */
    @Column(name = "points_required")
    private Integer pointsRequired;

    /**
     * The total quantity of this reward available across the entire event.
     */
    @Column(name = "total_quantity")
    private Integer totalQuantity;

    /**
     * The current remaining quantity of this reward available to be won.
     */
    @Column(name = "remaining_quantity")
    private Integer remainingQuantity;

    /**
     * The maximum number of this reward that can be distributed per day.
     */
    @Column(name = "daily_limit")
    private Integer dailyLimit;

    /**
     * The count of how many of this reward have been distributed today.
     * This is reset daily and used to enforce daily limits.
     */
    @Column(name = "daily_count")
    @Builder.Default
    private Integer dailyCount = 0;

    /**
     * The probability of winning this specific reward when spinning.
     * This can override the location's default win probability.
     */
    @Column(name = "win_probability")
    private Double winProbability;

    /**
     * The date and time when this reward becomes available.
     */
    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    /**
     * The date and time when this reward is no longer available.
     */
    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    /**
     * Additional metadata stored as a JSON string for extensibility.
     */
    @Column(name = "metadata")
    private String metadata;

    /**
     * The collection of spin history records associated with this reward.
     * This establishes the one-to-many relationship with the SpinHistory entity.
     */
    @OneToMany(mappedBy = "reward", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp DESC")
    @Builder.Default
    private Set<SpinHistory> spinHistories = new LinkedHashSet<>();

    /**
     * Gets the event associated with this reward through its event location.
     * 
     * @return the event, or null if no event location is set
     */
    public Event getEvent() {
        return eventLocation != null ? eventLocation.getEvent() : null;
    }

    /**
     * Gets the daily count with null safety.
     * 
     * @return the number of rewards distributed today, or 0 if null
     */
    public Integer getDailyCount() {
        return dailyCount != null ? dailyCount : 0;
    }

    /**
     * Increments the daily count of rewards distributed.
     * This is called when a participant wins this reward.
     */
    public void incrementDailyCount() {
        this.dailyCount = getDailyCount() + 1;
    }

    /**
     * Resets the daily count to zero.
     * This is typically called at the start of a new day.
     */
    public void resetDailyLimit() {
        this.dailyCount = 0;
    }

    /**
     * Resets the remaining quantity to the total quantity.
     * This is typically called when restocking rewards.
     */
    public void resetQuantity() {
        remainingQuantity = totalQuantity;
    }

    /**
     * Checks if there is any remaining quantity of this reward available.
     * 
     * @return true if there is at least one reward remaining, false otherwise
     */
    public boolean hasAvailableQuantity() {
        return remainingQuantity != null && remainingQuantity > 0;
    }

    /**
     * Decrements the remaining quantity and increments the daily count.
     * This is called when a participant wins this reward.
     */
    public void decrementRemainingQuantity() {
        if (remainingQuantity != null && remainingQuantity > 0) {
            remainingQuantity--;
            incrementDailyCount();
        }
    }

    /**
     * Determines if this reward is currently active and available to be won.
     * A reward is active if its base status is active, its event location is active,
     * it has remaining quantity, is within its validity period, and hasn't reached its daily limit.
     * 
     * @return true if the reward is active and available, false otherwise
     */
    @Override
    public boolean isActive() {
        // Check base status
        boolean baseActive = super.isActive();
        if (!baseActive) {
            System.out.println("Reward base status check failed");
            return false;
        }

        // Check event location
        if (eventLocation == null || !eventLocation.isActive()) {
            System.out.println("Reward event location check failed");
            System.out.println("EventLocation: " + (eventLocation != null ? eventLocation.getStatusName() : "null"));
            return false;
        }

        // Check remaining quantity
        if (remainingQuantity == null || remainingQuantity <= 0) {
            System.out.println("Reward quantity check failed: " + remainingQuantity);
            return false;
        }

        // Check time range
        if (validFrom == null || validUntil == null) {
            System.out.println("Reward time range null check failed");
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean timeValid = (now.isEqual(validFrom) || now.isAfter(validFrom)) &&
                (now.isEqual(validUntil) || now.isBefore(validUntil));

        if (!timeValid) {
            System.out.println("Reward time range check failed");
            System.out.println("now: " + now);
            System.out.println("validFrom: " + validFrom);
            System.out.println("validUntil: " + validUntil);
            return false;
        }

        // Check daily limit
        boolean dailyLimitValid = dailyLimit == null || getDailyCount() < dailyLimit;
        if (!dailyLimitValid) {
            System.out.println("Reward daily limit check failed");
            System.out.println("dailyCount: " + getDailyCount());
            System.out.println("dailyLimit: " + dailyLimit);
        }

        return dailyLimitValid;
    }

    /**
     * Returns a string representation of this reward.
     * 
     * @return a string containing the reward's ID, code, name, and location code
     */
    @Override
    public String toString() {
        return String.format("Reward[id=%d, code=%s, name=%s, location=%s]",
                id,
                code,
                name,
                eventLocation != null ? eventLocation.getCode() : "null"
        );
    }
}