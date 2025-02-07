package vn.com.fecredit.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Participant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Reward> rewards = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SpinHistory> spinHistories = new ArrayList<>();

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addParticipant(Participant participant) {
        this.participants.add(participant);
        participant.setEvent(this);
    }

    public void removeParticipant(Participant participant) {
        this.participants.remove(participant);
        participant.setEvent(null);
    }

    public void addReward(Reward reward) {
        this.rewards.add(reward);
        reward.setEvent(this);
    }

    public void removeReward(Reward reward) {
        this.rewards.remove(reward);
        reward.setEvent(null);
    }

    public void addSpinHistory(SpinHistory spinHistory) {
        this.spinHistories.add(spinHistory);
        spinHistory.setEvent(this);
    }

    public void removeSpinHistory(SpinHistory spinHistory) {
        this.spinHistories.remove(spinHistory);
        spinHistory.setEvent(null);
    }
}