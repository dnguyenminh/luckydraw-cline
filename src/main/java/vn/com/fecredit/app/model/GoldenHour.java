package vn.com.fecredit.app.model;

import java.time.LocalDateTime;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "golden_hours")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoldenHour {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "name")
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "multiplier", nullable = false)
    private Double multiplier = 1.0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "version")
    private Long version = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    // Hour-based access methods for backward compatibility
    @Transient
    public Integer getStartHour() {
        return startTime != null ? startTime.getHour() : null;
    }

    public void setStartHour(Integer hour) {
        if (hour != null) {
            if (startTime == null) {
                startTime = LocalDateTime.now();
            }
            startTime = startTime.withHour(hour).withMinute(0).withSecond(0).withNano(0);
        }
    }

    @Transient
    public Integer getEndHour() {
        return endTime != null ? endTime.getHour() : null;
    }

    public void setEndHour(Integer hour) {
        if (hour != null) {
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            endTime = endTime.withHour(hour).withMinute(0).withSecond(0).withNano(0);
        }
    }

    public boolean isWithinTimeRange(LocalDateTime dateTime) {
        if (startTime == null || endTime == null || dateTime == null) {
            return false;
        }

        int currentHour = dateTime.getHour();
        int startHour = startTime.getHour();
        int endHour = endTime.getHour();
        
        if (startHour <= endHour) {
            return currentHour >= startHour && currentHour < endHour;
        } else {
            // Handles cases crossing midnight (e.g., 22:00 - 03:00)
            return currentHour >= startHour || currentHour < endHour;
        }
    }

    public static GoldenHourBuilder builder() {
        return new GoldenHourBuilder();
    }

    public static class GoldenHourBuilder {
        private Long id;
        private Reward reward;
        private Event event;
        private String name;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Double multiplier = 1.0;
        private Boolean isActive = true;
        private Long version = 0L;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        GoldenHourBuilder() {
        }

        public GoldenHourBuilder startHour(Integer hour) {
            if (hour != null) {
                LocalDateTime time = LocalDateTime.now()
                    .withHour(hour)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
                return startTime(time);
            }
            return this;
        }

        public GoldenHourBuilder endHour(Integer hour) {
            if (hour != null) {
                LocalDateTime time = LocalDateTime.now()
                    .withHour(hour)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
                return endTime(time);
            }
            return this;
        }

        public GoldenHourBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public GoldenHourBuilder reward(Reward reward) {
            this.reward = reward;
            return this;
        }

        public GoldenHourBuilder event(Event event) {
            this.event = event;
            return this;
        }

        public GoldenHourBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GoldenHourBuilder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public GoldenHourBuilder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public GoldenHourBuilder multiplier(Double multiplier) {
            this.multiplier = multiplier;
            return this;
        }

        public GoldenHourBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public GoldenHourBuilder version(Long version) {
            this.version = version;
            return this;
        }

        public GoldenHourBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public GoldenHourBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public GoldenHour build() {
            return new GoldenHour(id, reward, event, name, startTime, endTime, 
                               multiplier, isActive, version, createdAt, updatedAt);
        }
    }
}