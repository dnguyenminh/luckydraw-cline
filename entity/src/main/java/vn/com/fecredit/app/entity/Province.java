package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing a geographical province in the lucky draw system.
 * Provinces are contained within regions and can have participants and spin histories.
 * <p>
 * Provinces provide a way to organize participants geographically and track spin activity
 * at a provincial level. They enforce hierarchical activation/deactivation rules with their
 * parent region and contained participants.
 */
@Entity
@Table(name = "provinces")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Province extends AbstractStatusAwareEntity {

    /**
     * The name of the province displayed to users.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Unique code identifier for the province, used in APIs and references.
     */
    @Column(nullable = false, length = 20, unique = true)
    private String code;

    /**
     * Detailed description of the province.
     */
    @Column(name = "description")
    private String description;

    /**
     * The region that contains this province.
     * This establishes the many-to-one relationship with the Region entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    @ToString.Exclude
    private Region region;

    /**
     * The collection of participant event records associated with this province.
     * This establishes the one-to-many relationship with the ParticipantEvent entity.
     */
    @OneToMany(mappedBy = "province")
    @Builder.Default
    @ToString.Exclude
    private Set<ParticipantEvent> participantEvents = new LinkedHashSet<>();

    /**
     * The collection of participants residing in this province.
     * This establishes the one-to-many relationship with the Participant entity.
     */
    @OneToMany(mappedBy = "province")
    @Builder.Default
    @ToString.Exclude
    private Set<Participant> participants = new HashSet<>();

    /**
     * The collection of spin history records associated with this province.
     * This establishes the one-to-many relationship with the SpinHistory entity.
     */
    @OneToMany(mappedBy = "province")
    @Builder.Default
    @ToString.Exclude
    private Set<SpinHistory> spinHistories = new LinkedHashSet<>();

    /**
     * Custom logic executed when this entity is activated.
     * Ensures that the parent region is also active.
     * 
     * @throws IllegalStateException if the region is inactive
     */
    @Override
    protected void onActivate() {
        if (region == null || !region.isActive()) {
            throw new IllegalStateException("Cannot activate province for inactive region");
        }
    }

    /**
     * Custom logic executed when this entity is deactivated.
     * Prevents deactivation if there are any active participants in this province.
     * 
     * @throws IllegalStateException if there are active participants
     */
    @Override
    protected void onDeactivate() {
        if (!participantEvents.isEmpty() && 
            participantEvents.stream().anyMatch(pe -> pe.isActive())) {
            throw new IllegalStateException("Cannot deactivate province with active participants");
        }
    }

    /**
     * Validates the state of this entity before persistence or update operations.
     * Ensures that required fields are set and the code is uppercase.
     * 
     * @throws IllegalStateException if validation fails
     */
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

    /**
     * Checks if this province has any active participants.
     * 
     * @return true if there is at least one active participant, false otherwise
     */
    public boolean hasActiveParticipants() {
        return !participantEvents.isEmpty() && 
               participantEvents.stream().anyMatch(pe -> pe.isActive());
    }

    /**
     * Gets the number of active participants in this province.
     * 
     * @return the count of active participants
     */
    public int getActiveParticipantsCount() {
        return (int) participantEvents.stream()
                .filter(pe -> pe.isActive())
                .count();
    }

    /**
     * Gets the total number of spins that have occurred in this province.
     * 
     * @return the count of spin histories
     */
    public int getTotalSpinsCount() {
        return spinHistories.size();
    }

    /**
     * Gets the number of winning spins that have occurred in this province.
     * 
     * @return the count of winning spin histories
     */
    public int getWinningSpinsCount() {
        return (int) spinHistories.stream()
                .filter(SpinHistory::isWin)
                .count();
    }

    /**
     * Calculates the win rate (ratio of winning spins to total spins) for this province.
     * 
     * @return the win rate as a decimal between 0.0 and 1.0, or 0.0 if no spins have occurred
     */
    public double getWinRate() {
        int totalSpins = getTotalSpinsCount();
        return totalSpins > 0 ? (double) getWinningSpinsCount() / totalSpins : 0.0;
    }

    /**
     * Gets the participants residing in this province.
     * 
     * @return the set of participants
     */
    public Set<Participant> getParticipants() {
        return participants;
    }
}
