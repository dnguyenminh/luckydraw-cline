package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "spin_histories")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SpinHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id")
    private EventLocation eventLocation;

    @Column(name = "spin_time", nullable = false)
    private LocalDateTime spinTime;

    @Column(name = "spin_date")
    private LocalDateTime spinDate;

    @Column(name = "is_win")
    private boolean win;

    @Column(name = "win_probability")
    private Double winProbability;

    @Column(name = "final_probability")
    private Double finalProbability;

    @Column(name = "is_golden_hour_active")
    private boolean goldenHourActive;

    @Column(name = "golden_hour_multiplier")
    private Double goldenHourMultiplier;

    @Column(name = "probability_multiplier")
    private Double probabilityMultiplier;

    @Column(length = 500)
    private String notes;

    public boolean isWin() {
        return win;
    }

    public void markWin(Reward reward) {
        this.win = true;
        this.reward = reward;
    }
    
    public void setGoldenHourDetails(boolean active, Double multiplier) {
        this.goldenHourActive = active;
        this.goldenHourMultiplier = multiplier;
    }

    public void setSpinTime(LocalDateTime spinTime) {
        this.spinTime = spinTime;
        this.spinDate = spinTime;
    }

    public void updateProbabilities(Double baseWinProbability, Double multiplier) {
        this.winProbability = baseWinProbability;
        this.probabilityMultiplier = multiplier;
        this.finalProbability = baseWinProbability * (multiplier != null ? multiplier : 1.0);
    }

    public LocalDateTime getSpinDate() {
        return this.spinDate != null ? this.spinDate : this.spinTime;
    }
}
