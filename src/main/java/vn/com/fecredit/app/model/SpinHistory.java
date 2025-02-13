package vn.com.fecredit.app.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "spin_histories", indexes = {
    @Index(name = "idx_spin_history_event", columnList = "event_id"),
    @Index(name = "idx_spin_history_participant", columnList = "participant_id"),
    @Index(name = "idx_spin_history_spin_time", columnList = "spin_time")
})
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

    @Column(name = "spin_time", nullable = false)
    private LocalDateTime spinTime;

    @Column(name = "result")
    private String result;

    @Column(name = "won")
    private Boolean won;

    @Column(name = "is_golden_hour")
    private Boolean isGoldenHour;

    @Column(name = "current_multiplier")
    private Double currentMultiplier;

    @Column(name = "remaining_spins")
    private Long remainingSpins;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public void setEvent(Event event) {
        this.event = event;
        if (event != null && !event.getSpinHistories().contains(this)) {
            event.getSpinHistories().add(this);
        }
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
        if (participant != null && !participant.getSpinHistories().contains(this)) {
            participant.getSpinHistories().add(this);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.spinTime == null) {
            this.spinTime = LocalDateTime.now();
        }
        if (this.won == null) {
            this.won = false;
        }
        if (this.isGoldenHour == null) {
            this.isGoldenHour = false;
        }
        if (this.currentMultiplier == null) {
            this.currentMultiplier = 1.0;
        }
        if (this.remainingSpins == null) {
            this.remainingSpins = 0L;
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = this.createdAt;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpinHistory)) return false;
        SpinHistory that = (SpinHistory) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}