package vn.com.fecredit.app.entity;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

/**
 * Entity representing a participant in the lucky draw system.
 * Participants are users who can join events, spin for rewards, and accumulate points.
 * <p>
 * Each participant can be associated with a province, have multiple roles, and participate
 * in multiple events. The entity tracks all participant activity across events and manages
 * their engagement with the lucky draw system.
 */
@Entity
@Table(name = "participants")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Participant extends AbstractStatusAwareEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for the participant.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The participant's account identifier, used for authentication.
     * Must be unique across all participants.
     */
    @Column(name = "account", nullable = false, unique = true)
    private String account;

    /**
     * The display name of the participant.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * The participant's phone number, used for contact and verification.
     * Must be unique across all participants.
     */
    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    /**
     * The participant's email address, used for notifications.
     */
    @Column(name = "email")
    private String email;

    /**
     * Unique code identifier for the participant, used in APIs and references.
     */
    @Column(name = "code")
    private String code;

    /**
     * Additional metadata stored as a JSON string for extensibility.
     */
    @Column(name = "metadata")
    private String metadata;

    /**
     * The collection of roles assigned to this participant.
     * This establishes the many-to-many relationship with the Role entity.
     */
    @Setter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "participant_roles",
            joinColumns = @JoinColumn(name = "participant_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * The collection of event participation records for this participant.
     * This establishes the one-to-many relationship with the ParticipantEvent entity.
     */
    @OneToMany(mappedBy = "participant", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ParticipantEvent> participantEvents = new HashSet<>();

    /**
     * The province where this participant is located.
     * This establishes the many-to-one relationship with the Province entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    private Province province;

    /**
     * The user account associated with this participant.
     * This establishes the one-to-one relationship with the User entity.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Sets the province for this participant, maintaining the bidirectional relationship.
     * Removes this participant from its previous province (if any) and adds it to the new one.
     * 
     * @param newProvince the province to associate with this participant
     */
    public void setProvince(Province newProvince) {
        Province oldProvince = this.province;
        
        // Remove from old province
        if (oldProvince != null && oldProvince.getParticipants().contains(this)) {
            oldProvince.getParticipants().remove(this);
        }

        this.province = newProvince;

        // Add to new province
        if (newProvince != null && !newProvince.getParticipants().contains(this)) {
            newProvince.getParticipants().add(this);
        }
    }

    /**
     * Joins an event using its default location.
     * Creates a new ParticipantEvent record if the participant is not already in the event.
     * 
     * @param event the event to join
     * @param initialSpins the initial number of spins to allocate to the participant
     * @return the ParticipantEvent record, or null if the event has no locations
     */
    public ParticipantEvent joinEvent(Event event, int initialSpins) {
        if (event == null || event.getEventLocations().isEmpty()) {
            return null;
        }
        return joinEventLocation(event.getEventLocations().iterator().next(), initialSpins);
    }

    /**
     * Joins an event at a specific location.
     * Creates a new ParticipantEvent record if the participant is not already at this location.
     * 
     * @param eventLocation the event location to join
     * @param initialSpins the initial number of spins to allocate to the participant
     * @return the ParticipantEvent record, or null if the location or event is invalid
     */
    public ParticipantEvent joinEventLocation(EventLocation eventLocation, int initialSpins) {
        if (eventLocation == null) {
            return null;
        }

        Event event = eventLocation.getEvent();
        if (event == null) {
            return null;
        }

        ParticipantEvent pe = participantEvents.stream()
            .filter(existingPe -> eventLocation.equals(existingPe.getEventLocation()))
            .findFirst()
            .orElse(null);

        if (pe != null) {
            return pe;
        }

        pe = ParticipantEvent.builder()
            .participant(this)
            .event(event)
            .eventLocation(eventLocation)
            .remainingSpins(initialSpins)
            .status(STATUS_ACTIVE)
            .build();

        participantEvents.add(pe);
        eventLocation.getParticipantEvents().add(pe);
        return pe;
    }

    /**
     * Leaves an event, removing all participation records for this event.
     * Also removes this participant from the event location's participant list.
     * 
     * @param event the event to leave
     */
    public void leaveEvent(Event event) {
        if (event == null) {
            return;
        }
        participantEvents.removeIf(pe -> {
            Event participantEvent = pe.getEvent();
            boolean matches = participantEvent != null && participantEvent.equals(event);
            if (matches) {
                EventLocation location = pe.getEventLocation();
                if (location != null) {
                    location.getParticipantEvents().remove(pe);
                }
            }
            return matches;
        });
    }

    /**
     * Gets the participant's participation record for a specific event.
     * 
     * @param event the event to check
     * @return the ParticipantEvent record, or null if the participant is not in the event
     */
    public ParticipantEvent getEventParticipation(Event event) {
        if (event == null) {
            return null;
        }
        for (ParticipantEvent pe : participantEvents) {
            Event participantEvent = pe.getEvent();
            if (participantEvent != null && participantEvent.equals(event)) {
                return pe;
            }
        }
        return null;
    }

    /**
     * Checks if the participant can spin in a specific event.
     * 
     * @param event the event to check
     * @return true if the participant can spin, false otherwise
     */
    public boolean canSpin(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        return pe != null && pe.canSpin();
    }

    /**
     * Increments the spin count for a specific event.
     * 
     * @param event the event to update
     */
    public void incrementSpinCount(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        if (pe != null) {
            pe.incrementSpinCount();
        }
    }

    /**
     * Increments the win count for a specific event.
     * 
     * @param event the event to update
     */
    public void incrementWinCount(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        if (pe != null) {
            pe.incrementWinCount();
        }
    }

    /**
     * Adds points to the participant's total for a specific event.
     * 
     * @param event the event to update
     * @param points the number of points to add
     */
    public void addPoints(Event event, int points) {
        ParticipantEvent pe = getEventParticipation(event);
        if (pe != null) {
            pe.addPoints(points);
        }
    }

    /**
     * Resets the daily spin count for a specific event.
     * 
     * @param event the event to update
     */
    public void resetDailySpinCount(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        if (pe != null) {
            pe.resetDailySpinCount();
        }
    }

    /**
     * Gets the daily spin count for a specific event.
     * 
     * @param event the event to check
     * @return the number of spins used today, or 0 if not participating
     */
    public int getDailySpinCount(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        return pe != null ? pe.getDailySpinCount() : 0;
    }

    /**
     * Gets the total number of spins for a specific event.
     * 
     * @param event the event to check
     * @return the total number of spins, or 0 if not participating
     */
    public int getTotalSpins(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        return pe != null ? pe.getTotalSpins() : 0;
    }

    /**
     * Gets the total number of wins for a specific event.
     * 
     * @param event the event to check
     * @return the total number of wins, or 0 if not participating
     */
    public int getTotalWins(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        return pe != null ? pe.getTotalWins() : 0;
    }

    /**
     * Gets the total number of points for a specific event.
     * 
     * @param event the event to check
     * @return the total number of points, or 0 if not participating
     */
    public int getTotalPoints(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        return pe != null ? pe.getTotalPoints() : 0;
    }
}
