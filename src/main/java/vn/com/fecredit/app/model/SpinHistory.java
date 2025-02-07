package vn.com.fecredit.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "spin_histories")
public class SpinHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    @Column(name = "result")
    private String result;

    @Column(name = "is_golden_hour")
    private Boolean isGoldenHour;

    @Column(name = "won")
    private Boolean won;

    @Column(name = "current_multiplier")
    private Double currentMultiplier;

    @Column(name = "remaining_spins")
    private Integer remainingSpins;

    @Column(name = "spin_time", nullable = false)
    private LocalDateTime spinTime;

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
        if (this.spinTime == null) {
            this.spinTime = LocalDateTime.now();
        }
        if (this.isGoldenHour == null) {
            this.isGoldenHour = false;
        }
        if (this.won == null) {
            this.won = false;
        }
        if (this.currentMultiplier == null) {
            this.currentMultiplier = 1.0;
        }
        if (this.remainingSpins == null) {
            this.remainingSpins = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}