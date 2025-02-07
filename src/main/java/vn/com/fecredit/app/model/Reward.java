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
@Table(name = "rewards")
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "remaining_quantity")
    private Integer remainingQuantity;

    @Column(name = "max_quantity_in_period")
    private Integer maxQuantityInPeriod;

    @Column(name = "probability")
    private Double probability;

    @Column(name = "applicable_provinces", length = 1000)
    private String applicableProvinces;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @OneToMany(mappedBy = "reward", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GoldenHour> goldenHours = new ArrayList<>();

    @OneToMany(mappedBy = "reward", cascade = CascadeType.ALL)
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
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (remainingQuantity == null) {
            remainingQuantity = quantity;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addGoldenHour(GoldenHour goldenHour) {
        goldenHours.add(goldenHour);
        goldenHour.setReward(this);
    }

    public void removeGoldenHour(GoldenHour goldenHour) {
        goldenHours.remove(goldenHour);
        goldenHour.setReward(null);
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public int getRemainingQuantity() {
        return remainingQuantity != null ? remainingQuantity : 0;
    }

    public void decrementRemainingQuantity() {
        if (remainingQuantity != null && remainingQuantity > 0) {
            remainingQuantity--;
        }
    }

    public boolean isAvailable(LocalDateTime time, String province) {
        if (!isActive || remainingQuantity <= 0) {
            return false;
        }

        if (startDate != null && time.isBefore(startDate)) {
            return false;
        }

        if (endDate != null && time.isAfter(endDate)) {
            return false;
        }

        if (applicableProvinces != null && !applicableProvinces.isEmpty() &&
                !applicableProvinces.contains(province)) {
            return false;
        }

        if (goldenHours != null && !goldenHours.isEmpty()) {
            return goldenHours.stream()
                    .anyMatch(gh -> gh.isActive() && gh.isWithinTimeRange(time));
        }

        return true;
    }

    public double getProbability() {
        return probability != null ? probability : 0.0;
    }
}
