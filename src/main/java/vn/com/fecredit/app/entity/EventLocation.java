package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "event_locations")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor 
public class EventLocation extends AbstractStatusAwareEntity implements Versionable {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String province;

    @Column
    private String district;

    @Column
    private String ward;
    
    @Column
    private String address;

    @Column
    private String city;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "event_id", insertable = false, updatable = false)
    private Long eventId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventLocation that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
