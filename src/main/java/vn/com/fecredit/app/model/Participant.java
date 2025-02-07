package vn.com.fecredit.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "full_name")
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
    private Integer spinsRemaining;

    @Column(name = "daily_spin_limit")
    private Integer dailySpinLimit;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    @Builder.Default
    private List<SpinHistory> spinHistories = new ArrayList<>();

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
            this.spinsRemaining = 0;
        }
        if (this.dailySpinLimit == null) {
            this.dailySpinLimit = 0;
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
        if (this.spinsRemaining > 0) {
            this.spinsRemaining--;
        }
    }

    public int getSpinsRemaining() {
        return spinsRemaining != null ? spinsRemaining : 0;
    }

    public int getDailySpinLimit() {
        return dailySpinLimit != null ? dailySpinLimit : 0;
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