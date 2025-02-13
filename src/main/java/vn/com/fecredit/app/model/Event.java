package vn.com.fecredit.app.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder.Default;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events", indexes = {
    @Index(name = "idx_event_code", columnList = "code"),
    @Index(name = "idx_event_dates", columnList = "start_date,end_date")
})
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "total_spins", nullable = false)
    private Long totalSpins;

    @Column(name = "remaining_spins")
    private Long remainingSpins;

    @Column(name = "is_active")
    private Boolean isActive;

@CreatedDate
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;

@LastModifiedDate
@Column(name = "updated_at", nullable = false)
private LocalDateTime updatedAt;

@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
@Default
private Set<Reward> rewards = new HashSet<>();


    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @Default
    private Set<Participant> participants = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @Default
    private Set<SpinHistory> spinHistories = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @Default
    private Set<GoldenHour> goldenHours = new HashSet<>();

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.totalSpins == null) {
            this.totalSpins = 0L;
        }
        if (this.remainingSpins == null) {
            this.remainingSpins = this.totalSpins;
        }
    }

    public Long getTotalSpins() {
        return totalSpins != null ? totalSpins : 0L;
    }

    public Long getRemainingSpins() {
        return remainingSpins != null ? remainingSpins : 0L;
    }

    public void decrementRemainingSpins() {
        if (this.remainingSpins != null && this.remainingSpins > 0) {
            this.remainingSpins--;
        }
    }

    public boolean hasSpinsAvailable() {
        return getRemainingSpins() > 0;
    }

    public boolean isInProgress() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate) && now.isBefore(endDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return id != null && id.equals(event.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public void addReward(Reward reward) {
        rewards.add(reward);
        reward.setEvent(this);
    }

    public void removeReward(Reward reward) {
        rewards.remove(reward);
        reward.setEvent(null);
    }

    public Set<Participant> getParticipants() {
        return participants;
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
        participant.setEvent(this);
    }

    public void removeParticipant(Participant participant) {
        participants.remove(participant);
        participant.setEvent(null);
    }

    public Set<SpinHistory> getSpinHistories() {
        return spinHistories;
    }

    public void addSpinHistory(SpinHistory spinHistory) {
        spinHistories.add(spinHistory);
        spinHistory.setEvent(this);
    }

    public void removeSpinHistory(SpinHistory spinHistory) {
        spinHistories.remove(spinHistory);
        spinHistory.setEvent(null);
    }

    public Set<GoldenHour> getGoldenHours() {
        return goldenHours;
    }

    public void addGoldenHour(GoldenHour goldenHour) {
        goldenHours.add(goldenHour);
        goldenHour.setEvent(this);
    }

    public void removeGoldenHour(GoldenHour goldenHour) {
        goldenHours.remove(goldenHour);
        goldenHour.setEvent(null);
    }
}