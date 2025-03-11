package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "spin_histories")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SpinHistory extends AbstractStatusAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_event_id", nullable = false)
    @ToString.Exclude
    private ParticipantEvent participantEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    @ToString.Exclude
    private Reward reward;

    @Column(name = "win")
    private Boolean win;

    @Column(name = "points_earned")
    private Integer pointsEarned;

    @Column(name = "finalized")
    private Boolean finalized;

    @Column(name = "spin_time")
    private LocalDateTime spinTime;

    @Column(name = "metadata")
    private String metadata;

    @PrePersist
    public void prePersist() {
        if (spinTime == null) {
            spinTime = LocalDateTime.now();
        }
        if (finalized == null) {
            finalized = false;
        }
        if (win == null) {
            win = false;
        }
    }

    @PreUpdate
    protected void validateState() {
        if (participantEvent == null) {
            throw new IllegalStateException("Participant event is required");
        }
        if (pointsEarned != null && pointsEarned < 0) {
            throw new IllegalStateException("Points earned cannot be negative");
        }
    }

    public boolean isWin() {
        return Boolean.TRUE.equals(win);
    }

    public boolean isFinalized() {
        return Boolean.TRUE.equals(finalized);
    }

    public void finalize() {
        if (isFinalized()) {
            throw new IllegalStateException("Spin history already finalized");
        }
        finalized = true;
    }

    public void markAsWin(Reward reward, Integer pointsEarned) {
        if (isFinalized()) {
            throw new IllegalStateException("Cannot modify finalized spin history");
        }
        this.reward = reward;
        this.pointsEarned = pointsEarned;
        this.win = true;
    }

    public void markAsLoss() {
        if (isFinalized()) {
            throw new IllegalStateException("Cannot modify finalized spin history");
        }
        this.reward = null;
        this.pointsEarned = 0;
        this.win = false;
    }
}
