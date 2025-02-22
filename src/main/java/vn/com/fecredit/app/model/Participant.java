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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "participants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "event_location_id")
    private EventLocation eventLocation;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "province")
    private String province;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "employee_id")
    private String employeeId;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "spins_remaining", nullable = false)
    @Builder.Default
    private Long spinsRemaining = 0L; // default value set to 0

    @Column(name = "daily_spin_limit")
    private Long dailySpinLimit;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_eligible_for_spin")
    @Builder.Default
    private Boolean isEligibleForSpin = true;

    @OneToMany(mappedBy = "participant")
    @Builder.Default
    private Set<SpinHistory> spinHistories = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        this.isActive = active;
    }

    public Boolean getIsEligibleForSpin() {
        return isEligibleForSpin;
    }

    public void setIsEligibleForSpin(Boolean eligible) {
        this.isEligibleForSpin = eligible;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isEligibleForSpin() {
        return Boolean.TRUE.equals(isEligibleForSpin);
    }
}