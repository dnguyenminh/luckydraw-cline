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
@Table(name = "lucky_draw_results")
public class LuckyDrawResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
        this.isClaimed = true;
        this.claimedAt = LocalDateTime.now();
        this.claimedBy = claimedBy;
        this.claimNotes = notes;
    }
}