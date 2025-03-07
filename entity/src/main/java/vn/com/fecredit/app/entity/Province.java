package vn.com.fecredit.app.entity;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

@Entity
@Table(name = "provinces")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Province extends AbstractStatusAwareEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "metadata")
    private String metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @OneToMany(mappedBy = "province", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<EventLocation> eventLocations = new HashSet<>();

    @OneToMany(mappedBy = "province", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Participant> participants = new HashSet<>();


    // Participant management
    public void addParticipant(Participant participant) {
        participants.add(participant);
        if (participant != null) {
            participant.setProvince(this);
        }
    }

    public void removeParticipant(Participant participant) {
        participants.remove(participant);
        if (participant != null && participant.getProvince() == this) {
            participant.setProvince(null);
        }
    }

    @Override
    public String toString() {
        return String.format("Province[id=%d, code=%s, name=%s, region=%s, locations=%d, participants=%d]",
            id,
            code,
            name,
            region != null ? region.getCode() : "null",
            eventLocations.size(),
            participants.size()
        );
    }
}
