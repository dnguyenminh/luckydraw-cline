package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

@Entity
@Table(name = "participant_events")
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantEvent extends AbstractStatusAwareEntity {

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

    @Setter
    @Column(name = "available_spins")
    @Builder.Default
    private Integer availableSpins = 0;

    @Column(name = "daily_spin_count")
    @Builder.Default
    private int dailySpinCount = 0;

    @Setter
    @Column(name = "total_spins")
    @Builder.Default
    private int totalSpins = 0;

    @Setter
    @Column(name = "total_wins")
    @Builder.Default
    private int totalWins = 0;

    @Setter
    @Column(name = "total_points")
    @Builder.Default
    private int totalPoints = 0;

    @Column(name = "metadata")
    private String metadata;

    public Event getEvent() {
        return eventLocation != null ? eventLocation.getEvent() : null;
    }

    public void setEventLocation(EventLocation newLocation) {
        if (this.eventLocation != null && this.eventLocation.getParticipantEvents().contains(this)) {
            this.eventLocation.getParticipantEvents().remove(this);
        }
        this.eventLocation = newLocation;
        if (newLocation != null) {
            if (!newLocation.getParticipantEvents().contains(this)) {
                newLocation.getParticipantEvents().add(this);
            }
            // Initialize spins if needed
            if (availableSpins == null || availableSpins == 0) {
                Integer initialSpins = newLocation.getEffectiveInitialSpins();
                if (initialSpins != null) {
                    availableSpins = initialSpins;
                }
            }
        }
    }

    public void setParticipant(Participant newParticipant) {
        if (this.participant != null && this.participant.getParticipantEvents().contains(this)) {
            this.participant.getParticipantEvents().remove(this);
        }
        this.participant = newParticipant;
        if (newParticipant != null && !newParticipant.getParticipantEvents().contains(this)) {
            newParticipant.getParticipantEvents().add(this);
        }
    }

    public void incrementSpinCount() {
        if (!canSpin()) {
            return;
        }
        dailySpinCount++;
        if (totalSpins < Integer.MAX_VALUE) {
            totalSpins++;
        }
        if (availableSpins != null && availableSpins > 0) {
            availableSpins--;
        }
    }

    public void incrementWinCount() {
        if (totalWins < Integer.MAX_VALUE) {
            totalWins++;
        }
    }

    public void addPoints(Integer points) {
        if (points == null) {
            throw new IllegalArgumentException("Points cannot be null");
        }
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative");
        }
        if (totalPoints <= Integer.MAX_VALUE - points) {
            totalPoints += points;
        } else {
            totalPoints = Integer.MAX_VALUE;
        }
    }

    public void resetDailySpinCount() {
        dailySpinCount = 0;
    }

    public void addAvailableSpins(int spins) {
        if (spins < 0) {
            throw new IllegalArgumentException("Cannot add negative spins");
        }
        if (availableSpins == null) {
            availableSpins = spins;
        } else if (availableSpins <= Integer.MAX_VALUE - spins) {
            availableSpins += spins;
        } else {
            availableSpins = Integer.MAX_VALUE;
        }
    }

    public boolean canSpin() {
        if (!isActive() || eventLocation == null) {
            return false;
        }

        Integer limit = eventLocation.getEffectiveDailySpinLimit();
        return availableSpins != null && availableSpins > 0 &&
               (limit == null || dailySpinCount < limit);
    }

    @Override
    public boolean isActive() {
        return super.isActive() && 
               participant != null && participant.isActive() &&
               eventLocation != null && eventLocation.isActive() &&
               getEvent() != null && getEvent().isActive();
    }

    @Override
    public String toString() {
        Event event = getEvent();
        return String.format("ParticipantEvent[id=%d, participant=%s, event=%s]",
                id,
                participant != null ? participant.getCode() : "null",
                event != null ? event.getCode() : "null");
    }
}
