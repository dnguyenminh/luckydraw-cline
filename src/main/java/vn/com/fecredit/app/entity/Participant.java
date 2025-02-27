package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "participants")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Participant extends BaseEntity {

    @NotNull
    @Column(name = "event_id")
    private Long eventId;

    @NotNull
    @Column(name = "event_location_id")
    private Long eventLocationId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "customer_id", unique = true)
    private String customerId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "card_number", unique = true)
    private String cardNumber;

    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name")
    private String fullName;

    @Email
    @Size(max = 100)
    @Column(unique = true)
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    @Size(max = 15)
    @Column(name = "phone_number")
    private String phoneNumber;

    @Size(max = 100)
    private String province;

    @Column(name = "total_spins")
    private Integer totalSpins;

    @Column(name = "remaining_spins")
    private Integer remainingSpins;

    @Column(name = "daily_spin_limit")
    private Integer dailySpinLimit;

    @Column(name = "daily_spins_used")
    private Integer dailySpinsUsed;

    @Column(name = "last_spin_date")
    private LocalDateTime lastSpinDate;

    // @Column(name = "is_active")
    // private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id", insertable = false, updatable = false)
    private EventLocation eventLocation;

    @OneToMany(mappedBy = "participant")
    private Set<SpinHistory> spinHistories = new HashSet<>();

    public boolean hasSpinsRemaining() {
        return remainingSpins != null && remainingSpins > 0;
    }

    public boolean hasDailySpinsRemaining() {
        if (dailySpinLimit == null || dailySpinsUsed == null) {
            return true;
        }
        return dailySpinsUsed < dailySpinLimit;
    }

    public void decrementRemainingSpins() {
        if (hasSpinsRemaining()) {
            remainingSpins--;
        }
    }

    public void incrementDailySpinsUsed() {
        if (dailySpinsUsed == null) {
            dailySpinsUsed = 1;
        } else {
            dailySpinsUsed++;
        }
        lastSpinDate = LocalDateTime.now();
    }

    public void resetDailySpins() {
        dailySpinsUsed = 0;
    }

    public boolean isEligibleForSpin() {
        return isActive() && hasSpinsRemaining() && hasDailySpinsRemaining();
    }

    public void updateSpinLimits(Integer totalSpins, Integer dailyLimit) {
        if (totalSpins != null) {
            this.totalSpins = totalSpins;
            if (this.remainingSpins == null || this.remainingSpins > totalSpins) {
                this.remainingSpins = totalSpins;
            }
        }
        if (dailyLimit != null) {
            this.dailySpinLimit = dailyLimit;
        }
    }

    public void addSpinHistory(SpinHistory spinHistory) {
        spinHistories.add(spinHistory);
        spinHistory.setParticipant(this);
    }

    public void removeSpinHistory(SpinHistory spinHistory) {
        spinHistories.remove(spinHistory);
        spinHistory.setParticipant(null);
    }
}
