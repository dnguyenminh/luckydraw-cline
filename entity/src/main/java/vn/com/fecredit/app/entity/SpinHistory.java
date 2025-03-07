package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

@Entity
@Table(name = "spin_histories")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SpinHistory extends AbstractStatusAwareEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id")
    private EventLocation eventLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "golden_hour_id")
    private GoldenHour goldenHour;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "win")
    private Boolean win;

    @Column(name = "points_earned")
    @Builder.Default
    private Integer pointsEarned = 0;

    @Column(name = "points_spent")
    @Builder.Default
    private Integer pointsSpent = 0;

    @Column(name = "metadata")
    private String metadata;

    // Legacy support methods
    @Deprecated
    public Event getEvent() {
        return eventLocation != null ? eventLocation.getEvent() : null;
    }

    @Deprecated
    public void setEvent(Event event) {
        if (event != null && !event.getEventLocations().isEmpty()) {
            this.eventLocation = event.getDefaultLocation();
        }
    }

    public void setWin(Boolean win) {
        this.win = win;
        updatePoints();
    }

    public boolean isWin() {
        return Boolean.TRUE.equals(win);
    }

    private void updatePoints() {
        if (isWin() && reward != null) {
            this.pointsEarned = reward.getPoints() != null ? reward.getPoints() : 0;
            this.pointsSpent = reward.getPointsRequired() != null ? reward.getPointsRequired() : 0;
        } else {
            this.pointsEarned = 0;
            this.pointsSpent = 0;
        }
    }

    public void setReward(Reward reward) {
        this.reward = reward;
        updatePoints();
    }

    public Double calculateWinProbability() {
        if (goldenHour != null && goldenHour.isActive(timestamp)) {
            return goldenHour.getWinProbability();
        }
        return eventLocation != null ? eventLocation.getEffectiveDefaultWinProbability() : 0.0;
    }

    @Override
    public boolean isActive() {
        return super.isActive() &&
               participant != null && participant.isActive() &&
               eventLocation != null && eventLocation.isActive();
    }

    @Override
    public String toString() {
        return String.format("SpinHistory[id=%d, participant=%s, location=%s, reward=%s, win=%b, points=%d/%d, timestamp=%s]",
            id,
            participant != null ? participant.getCode() : "null",
            eventLocation != null ? eventLocation.getCode() : "null",
            reward != null ? reward.getCode() : "null",
            isWin(),
            pointsEarned,
            pointsSpent,
            timestamp
        );
    }
}
