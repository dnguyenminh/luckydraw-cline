package vn.com.fecredit.app.entity;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

@Entity
@Table(name = "participants")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Participant extends AbstractStatusAwareEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account", nullable = false, unique = true)
    private String account;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "code")
    private String code;

    @Column(name = "metadata")
    private String metadata;

    @Setter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "participant_roles",
            joinColumns = @JoinColumn(name = "participant_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "participant", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ParticipantEvent> participantEvents = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    private Province province;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

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

    public ParticipantEvent joinEvent(Event event, int initialSpins) {
        if (event == null || event.getEventLocations().isEmpty()) {
            return null;
        }
        return joinEventLocation(event.getEventLocations().iterator().next(), initialSpins);
    }

    public ParticipantEvent joinEventLocation(EventLocation eventLocation, int initialSpins) {
        if (eventLocation == null) {
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
            .eventLocation(eventLocation)
            .availableSpins(initialSpins)
            .status(STATUS_ACTIVE)
            .build();

        participantEvents.add(pe);
        eventLocation.getParticipantEvents().add(pe);
        return pe;
    }

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

    public boolean canSpin(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        return pe != null && pe.canSpin();
    }

    public void incrementSpinCount(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        if (pe != null) {
            pe.incrementSpinCount();
        }
    }

    public void incrementWinCount(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        if (pe != null) {
            pe.incrementWinCount();
        }
    }

    public void addPoints(Event event, int points) {
        ParticipantEvent pe = getEventParticipation(event);
        if (pe != null) {
            pe.addPoints(points);
        }
    }

    public void resetDailySpinCount(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        if (pe != null) {
            pe.resetDailySpinCount();
        }
    }

    public int getDailySpinCount(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        return pe != null ? pe.getDailySpinCount() : 0;
    }

    public int getTotalSpins(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        return pe != null ? pe.getTotalSpins() : 0;
    }

    public int getTotalWins(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        return pe != null ? pe.getTotalWins() : 0;
    }

    public int getTotalPoints(Event event) {
        ParticipantEvent pe = getEventParticipation(event);
        return pe != null ? pe.getTotalPoints() : 0;
    }
}
