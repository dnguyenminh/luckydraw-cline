package vn.com.fecredit.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "participants")
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "employee_id", nullable = false, unique = true)
    private String employeeId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "province")
    private String province;

    @Column(name = "department")
    private String department;

    @Column(name = "position")
    private String position;

    @Column(name = "spins_remaining")
    private Long spinsRemaining;

    @Column(name = "daily_spin_limit")
    private Long dailySpinLimit;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Event event;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<SpinHistory> spinHistories = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.spinsRemaining == null) {
            this.spinsRemaining = 0L;
        }
        if (this.dailySpinLimit == null) {
            this.dailySpinLimit = 0L;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void addSpinHistory(SpinHistory spinHistory) {
        this.spinHistories.add(spinHistory);
        spinHistory.setParticipant(this);
    }

    public void removeSpinHistory(SpinHistory spinHistory) {
        this.spinHistories.remove(spinHistory);
        spinHistory.setParticipant(null);
    }

    public void decrementSpinsRemaining() {
        if (this.spinsRemaining != null && this.spinsRemaining > 0) {
            this.spinsRemaining = this.spinsRemaining - 1L;
        }
    }

    public Long getSpinsRemaining() {
        return spinsRemaining != null ? spinsRemaining : 0L;
    }

    public Long getDailySpinLimit() {
        return dailySpinLimit != null ? dailySpinLimit : 0L;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getIsActive() {
        return isActive != null ? isActive : true;
    }
}