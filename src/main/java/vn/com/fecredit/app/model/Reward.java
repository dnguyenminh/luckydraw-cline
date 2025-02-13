package vn.com.fecredit.app.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder.Default;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rewards")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "remaining_quantity", nullable = false)
    private Integer remainingQuantity;

    @Column(name = "max_quantity_in_period")
    private Integer maxQuantityInPeriod;

    private Double probability;

    @Column(name = "applicable_provinces", length = 1000)
    private String applicableProvinces;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @OneToMany(mappedBy = "reward", cascade = CascadeType.ALL)
    @Default
    private Set<GoldenHour> goldenHours = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.remainingQuantity == null) {
            this.remainingQuantity = this.quantity;
        }
    }

    public boolean isAvailable(LocalDateTime dateTime, String province) {
        if (!Boolean.TRUE.equals(isActive)) {
            return false;
        }

        if (remainingQuantity <= 0) {
            return false;
        }

        if (startDate != null && dateTime.isBefore(startDate)) {
            return false;
        }

        if (endDate != null && dateTime.isAfter(endDate)) {
            return false;
        }

        if (applicableProvinces != null && !applicableProvinces.isEmpty() && province != null) {
            Set<String> provinces = new HashSet<>(Arrays.asList(applicableProvinces.split(",")));
            if (!provinces.contains(province.trim())) {
                return false;
            }
        }

        return true;
    }

    public void decrementRemainingQuantity() {
        if (this.remainingQuantity > 0) {
            this.remainingQuantity--;
        }
    }

    public void addGoldenHour(GoldenHour goldenHour) {
        goldenHours.add(goldenHour);
        if (goldenHour.getReward() != this) {
            goldenHour.setReward(this);
        }
    }

    public void removeGoldenHour(GoldenHour goldenHour) {
        goldenHours.remove(goldenHour);
        if (goldenHour.getReward() == this) {
            goldenHour.setReward(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reward)) return false;
        Reward reward = (Reward) o;
        return id != null && id.equals(reward.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
