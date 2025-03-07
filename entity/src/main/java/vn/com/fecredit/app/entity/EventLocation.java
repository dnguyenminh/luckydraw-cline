package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

@Entity
@Table(name = "event_locations")
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EventLocation extends AbstractStatusAwareEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "name")
    private String name;

    @Setter
    @Column(name = "code", unique = true)
    private String code;

    @Setter
    @Column(name = "description")
    private String description;

    @Setter
    @Column(name = "initial_spins")
    private Integer initialSpins;

    @Setter
    @Column(name = "daily_spin_limit")
    private Integer dailySpinLimit;

    @Setter
    @Column(name = "default_win_probability")
    private Double defaultWinProbability;

    @Setter
    @Column(name = "metadata")
    private String metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    private Province province;

    @OneToMany(mappedBy = "eventLocation", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ParticipantEvent> participantEvents = new HashSet<>();

    @OneToMany(mappedBy = "eventLocation", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<GoldenHour> goldenHours = new HashSet<>();

    @OneToMany(mappedBy = "eventLocation", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Reward> rewards = new HashSet<>();

    @OneToMany(mappedBy = "eventLocation", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<SpinHistory> spinHistories = new HashSet<>();

    public void setEvent(Event newEvent) {
        // Remove from old event
        if (this.event != null && this.event.getEventLocations().contains(this)) {
            this.event.getEventLocations().remove(this);
        }

        this.event = newEvent;

        // Add to new event and initialize configuration
        if (newEvent != null) {
            if (!newEvent.getEventLocations().contains(this)) {
                newEvent.getEventLocations().add(this);
            }
            // Initialize configuration from new event
            if (initialSpins == null) {
                this.initialSpins = newEvent.getInitialSpins();
            }
            if (dailySpinLimit == null) {
                this.dailySpinLimit = newEvent.getDailySpinLimit();
            }
            if (defaultWinProbability == null) {
                this.defaultWinProbability = newEvent.getDefaultWinProbability();
            }
        }
    }

    public void setRegion(Region newRegion) {
        Region oldRegion = this.region;

        // Remove from old region
        if (oldRegion != null && oldRegion.getEventLocations().contains(this)) {
            oldRegion.getEventLocations().remove(this);
        }

        this.region = newRegion;

        // Add to new region
        if (newRegion != null && !newRegion.getEventLocations().contains(this)) {
            newRegion.getEventLocations().add(this);
        }

        // Ensure no overlapping provinces in the same event
        if (newRegion != null && event != null) {
            if (event.hasOverlappingProvinces(this)) {
                throw new IllegalStateException("Cannot assign region - would create overlapping provinces within event");
            }
        }
    }

    public Integer getEffectiveInitialSpins() {
        return initialSpins != null ? initialSpins : 
               (event != null ? event.getInitialSpins() : null);
    }

    public Integer getEffectiveDailySpinLimit() {
        return dailySpinLimit != null ? dailySpinLimit :
               (event != null ? event.getDailySpinLimit() : null);
    }

    public Double getEffectiveDefaultWinProbability() {
        return defaultWinProbability != null ? defaultWinProbability :
               (event != null ? event.getDefaultWinProbability() : null);
    }

    @Override
    public boolean isActive() {
        // First check this location's base status
        boolean baseActive = super.isActive();
        if (!baseActive) {
            System.out.println("EventLocation base status check failed");
            System.out.println("Status: " + getStatus() + " (" + getStatusName() + ")");
            return false;
        }

        // Then check event
        if (event == null) {
            System.out.println("EventLocation event null check failed");
            return false;
        }

        boolean eventActive = event.isActive();
        if (!eventActive) {
            System.out.println("EventLocation event active check failed");
            System.out.println("Event status: " + event.getStatus() + " (" + event.getStatusName() + ")");
            System.out.println("Event start: " + event.getStartTime());
            System.out.println("Event end: " + event.getEndTime());
            System.out.println("Current time: " + LocalDateTime.now());
            return false;
        }

        // Finally check region
        if (region == null) {
            System.out.println("EventLocation region null check failed");
            return false;
        }

        boolean regionActive = region.isActive();
        if (!regionActive) {
            System.out.println("EventLocation region active check failed");
            System.out.println("Region status: " + region.getStatus() + " (" + region.getStatusName() + ")");
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return String.format("EventLocation[id=%d, code=%s, name=%s]",
                id, code, name);
    }
}
