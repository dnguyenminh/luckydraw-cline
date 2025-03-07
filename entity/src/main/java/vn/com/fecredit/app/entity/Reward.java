package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

@Entity
@Table(name = "rewards")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Reward extends AbstractStatusAwareEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id")
    private EventLocation eventLocation;

    @Column(name = "name")
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "points")
    private Integer points;

    @Column(name = "points_required")
    private Integer pointsRequired;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "remaining_quantity")
    private Integer remainingQuantity;

    @Column(name = "daily_limit")
    private Integer dailyLimit;

    @Column(name = "daily_count")
    @Builder.Default
    private Integer dailyCount = 0;

    @Column(name = "win_probability")
    private Double winProbability;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "metadata")
    private String metadata;

    public Event getEvent() {
        return eventLocation != null ? eventLocation.getEvent() : null;
    }

    public Integer getDailyCount() {
        return dailyCount != null ? dailyCount : 0;
    }

    public void incrementDailyCount() {
        this.dailyCount = getDailyCount() + 1;
    }

    public void resetDailyLimit() {
        this.dailyCount = 0;
    }

    public void resetQuantity() {
        remainingQuantity = totalQuantity;
    }

    public boolean hasAvailableQuantity() {
        return remainingQuantity != null && remainingQuantity > 0;
    }

    public void decrementRemainingQuantity() {
        if (remainingQuantity != null && remainingQuantity > 0) {
            remainingQuantity--;
            incrementDailyCount();
        }
    }

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
