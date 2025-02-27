package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rewards")
public class Reward extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_date", nullable = false) 
    private LocalDateTime endTime;

    @Column(name = "win_probability", nullable = false)
    private Double winProbability;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "remaining_quantity")
    private Integer remainingQuantity;

    @Column(name = "daily_limit")  
    private Integer dailyLimit;

    @OneToMany(mappedBy = "reward")
    private Set<GoldenHour> goldenHours = new HashSet<>();

    @OneToMany(mappedBy = "reward")
    private Set<SpinHistory> spinHistories = new HashSet<>();

    /**
     * Get base win probability
     */
    public double getProbability() {
        return winProbability != null ? winProbability : 0.0;
    }

    /**
     * Get effective probability considering golden hour multipliers
     */
    public double getEffectiveProbability() {
        double multiplier = goldenHours.stream()
            .filter(GoldenHour::isActive)
            .mapToDouble(GoldenHour::getProbabilityMultiplier)
            .max()
            .orElse(1.0);
            
        return getProbability() * multiplier;
    }

    public void setProbability(Double probability) {
        this.winProbability = probability;
    }

    public Integer getQuantity() {
        return this.totalQuantity;
    }

    public void updateQuantity(Integer newQuantity) {
        this.totalQuantity = newQuantity;
        if (this.remainingQuantity == null || this.remainingQuantity > newQuantity) {
            this.remainingQuantity = newQuantity;
        }
    }

    public LocalDateTime getStartDate() {
        return this.startTime;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startTime = startDate;
    }

    public LocalDateTime getEndDate() {
        return this.endTime;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endTime = endDate;
    }

    /**
     * Check if reward is currently active
     */
    public boolean isActive() {
        return isActiveAt(LocalDateTime.now());
    }

    /**
     * Check if reward is active at specific time
     */
    public boolean isActiveAt(LocalDateTime checkTime) {
        if (!super.getStatus().isActive()) {
            return false;
        }

        return startTime.isBefore(checkTime) && 
               endTime.isAfter(checkTime) && 
               (remainingQuantity == null || remainingQuantity > 0);
    }

    /**
     * Check if reward is available
     */
    public boolean hasAvailableQuantity() {
        return remainingQuantity == null || remainingQuantity > 0; 
    }

    /**
     * Get win probability for this reward
     */
    public double getWinProbability() {
        return getEffectiveProbability();
    }

    /**
     * Decrement remaining quantity by 1
     */
    public void decrementRemainingQuantity() {
        if (remainingQuantity != null && remainingQuantity > 0) {
            remainingQuantity--;
        }
    }

    public void addGoldenHour(GoldenHour goldenHour) {
        goldenHours.add(goldenHour);
        goldenHour.setReward(this);
    }

    public void removeGoldenHour(GoldenHour goldenHour) {
        goldenHours.remove(goldenHour);
        goldenHour.setReward(null);
    }

    public void addSpinHistory(SpinHistory spinHistory) {
        spinHistories.add(spinHistory);
        spinHistory.setReward(this);
    }

    public void removeSpinHistory(SpinHistory spinHistory) {
        spinHistories.remove(spinHistory);
        spinHistory.setReward(null);
    }
}
