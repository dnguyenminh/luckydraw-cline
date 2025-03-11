package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "provinces")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Province extends AbstractStatusAwareEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    @ToString.Exclude
    private Region region;

    @OneToMany(mappedBy = "province")
    @Builder.Default
    @ToString.Exclude
    private List<ParticipantEvent> participantEvents = new ArrayList<>();

    @OneToMany(mappedBy = "province")
    @Builder.Default
    @ToString.Exclude
    private List<SpinHistory> spinHistories = new ArrayList<>();

    @Override
    protected void onActivate() {
        if (region == null || !region.isActive()) {
            throw new IllegalStateException("Cannot activate province for inactive region");
        }
    }

    @Override
    protected void onDeactivate() {
        if (!participantEvents.isEmpty() && 
            participantEvents.stream().anyMatch(pe -> pe.isActive())) {
            throw new IllegalStateException("Cannot deactivate province with active participants");
        }
    }

    @PrePersist
    @PreUpdate
    public void validateState() {
        if (code != null) {
            code = code.toUpperCase();
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Name is required");
        }
        
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalStateException("Code is required");
        }

        if (region == null) {
            throw new IllegalStateException("Region is required");
        }
    }

    public boolean hasActiveParticipants() {
        return !participantEvents.isEmpty() && 
               participantEvents.stream().anyMatch(pe -> pe.isActive());
    }

    public int getActiveParticipantsCount() {
        return (int) participantEvents.stream()
                .filter(pe -> pe.isActive())
                .count();
    }

    public int getTotalSpinsCount() {
        return spinHistories.size();
    }

    public int getWinningSpinsCount() {
        return (int) spinHistories.stream()
                .filter(SpinHistory::isWin)
                .count();
    }

    public double getWinRate() {
        int totalSpins = getTotalSpinsCount();
        return totalSpins > 0 ? (double) getWinningSpinsCount() / totalSpins : 0.0;
    }
}
