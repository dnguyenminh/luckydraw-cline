package vn.com.fecredit.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lucky_draw_results", 
      indexes = {
          @Index(name = "idx_lucky_draw_reward_pack", columnList = "reward_id,pack_number"),
          @Index(name = "idx_lucky_draw_win_time", columnList = "win_time")
      })
public class LuckyDrawResult {
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
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spin_history_id", nullable = false)
    private SpinHistory spinHistory;

    @Column(name = "win_time", nullable = false)
    private LocalDateTime winTime;

    @Column(name = "pack_number", nullable = false)
    private Integer packNumber;

    @Column(name = "is_claimed")
    private Boolean isClaimed;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @Column(name = "claimed_by")
    private String claimedBy;

    @Column(name = "claim_notes")
    private String claimNotes;

    @Version
    private Long version;

    public void claim(String claimedBy, String notes) {
        if (Boolean.TRUE.equals(this.isClaimed)) {
            throw new IllegalStateException("Result already claimed");
        }
        this.isClaimed = true;
        this.claimedAt = LocalDateTime.now();
        this.claimedBy = claimedBy;
        this.claimNotes = notes;
    }

    @PrePersist
    protected void onCreate() {
        if (this.isClaimed == null) {
            this.isClaimed = false;
        }
        if (this.winTime == null) {
            this.winTime = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LuckyDrawResult)) return false;
        LuckyDrawResult that = (LuckyDrawResult) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "LuckyDrawResult(id=" + id +
               ", packNumber=" + packNumber +
               ", winTime=" + winTime +
               ", isClaimed=" + isClaimed + ")";
    }
}