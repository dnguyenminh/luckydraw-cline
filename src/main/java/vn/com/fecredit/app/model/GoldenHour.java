package vn.com.fecredit.app.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder.Default;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "golden_hours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoldenHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    @Column(name = "multiplier")
    private Double multiplier;

    @Column(name = "is_active")
    @Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (multiplier == null) {
            multiplier = 1.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public Double getMultiplier() {
        return multiplier != null ? multiplier : 1.0;
    }

    public boolean isWithinTimeRange(LocalDateTime dateTime) {
        if (startTime == null || endTime == null) {
            return false;
        }
        LocalTime time = dateTime.toLocalTime();
        if (startTime.isBefore(endTime)) {
            // Normal case: start < end (e.g., 09:00-17:00)
            return !time.isBefore(startTime.toLocalTime()) && time.isBefore(endTime.toLocalTime());
        } else {
            // Cross-midnight case: start > end (e.g., 22:00-06:00)
            return !time.isBefore(startTime.toLocalTime()) || time.isBefore(endTime.toLocalTime());
        }
    }

    public LocalDateTime toLocalDateTime(LocalTime time, LocalDateTime referenceDate) {
        return referenceDate.withHour(time.getHour())
                          .withMinute(time.getMinute())
                          .withSecond(time.getSecond())
                          .withNano(time.getNano());
    }

    public LocalTime toLocalTime(LocalDateTime dateTime) {
        return dateTime.toLocalTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoldenHour)) return false;
        GoldenHour that = (GoldenHour) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "GoldenHour(id=" + id +
               ", name=" + name +
               ", startTime=" + startTime +
               ", endTime=" + endTime +
               ", multiplier=" + multiplier +
               ", isActive=" + isActive + ")";
    }
}