package vn.com.fecredit.app.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rewards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reward {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "event_region_id")
    private Long eventRegionId;

    @Column(name = "code")
    private String code;

    @NotNull(message = "Reward name must not be null")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "applicable_provinces", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Builder.Default
    private String[] applicableProvinces = new String[0];

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "remaining_quantity", nullable = false)
    @Builder.Default
    private Integer remainingQuantity = 0;

    @Column(name = "probability", nullable = false)
    @Builder.Default
    private Double probability = 0.0;

    @Column(name = "max_quantity_in_period")
    private Integer maxQuantityInPeriod;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;

    @OneToMany(mappedBy = "reward")
    @Builder.Default
    private Set<GoldenHour> goldenHours = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    public String getApplicableProvincesAsString() {
        return applicableProvinces == null ? "" : String.join(",", applicableProvinces);
    }

    public void setApplicableProvincesFromString(String provinces) {
        if (provinces == null || provinces.trim().isEmpty()) {
            this.applicableProvinces = new String[0];
        } else {
            this.applicableProvinces = provinces.split(",");
        }
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isAvailable() {
        return isAvailable(LocalDateTime.now(), null);
    }

    public boolean isAvailable(LocalDateTime checkTime, String province) {
        if (!isActive() || !hasRemainingQuantity() || !isWithinValidPeriod(checkTime)) {
            return false;
        }

        if (province != null && applicableProvinces != null && applicableProvinces.length > 0) {
            return Arrays.asList(applicableProvinces).contains(province);
        }

        return true;
    }

    public void addGoldenHour(GoldenHour goldenHour) {
        goldenHours.add(goldenHour);
        goldenHour.setReward(this);
    }

    public void removeGoldenHour(GoldenHour goldenHour) {
        goldenHours.remove(goldenHour);
        goldenHour.setReward(null);
    }

    public synchronized void decrementRemainingQuantity() {
        if (remainingQuantity > 0) {
            remainingQuantity--;
        }
    }

    private boolean hasRemainingQuantity() {
        return remainingQuantity != null && remainingQuantity > 0;
    }

    private boolean isWithinValidPeriod(LocalDateTime checkTime) {
        return (startDate == null || startDate.isBefore(checkTime)) 
            && (endDate == null || endDate.isAfter(checkTime));
    }
}
