package vn.com.fecredit.app.entity;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

@Entity
@Table(name = "roles")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AbstractStatusAwareEntity {

    private static final long serialVersionUID = 1L;

    public static final int STATUS_ACTIVE = AbstractStatusAwareEntity.STATUS_ACTIVE;
    public static final int STATUS_INACTIVE = AbstractStatusAwareEntity.STATUS_INACTIVE;

    {
        setStatus(STATUS_ACTIVE); // Initialize with active status
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "priority")
    @Builder.Default
    private int priority = 0;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Participant> participants = new HashSet<>();

    @Column(name = "metadata")
    private String metadata;

    public Set<User> getUsers() {
        if (users == null) {
            users = new HashSet<>();
        }
        return users;
    }

    public Set<Participant> getParticipants() {
        if (participants == null) {
            participants = new HashSet<>();
        }
        return participants;
    }

    public void setUsers(Set<User> users) {
        this.users = users != null ? users : new HashSet<>();
    }

    public void setParticipants(Set<Participant> participants) {
        this.participants = participants != null ? participants : new HashSet<>();
    }

    public void addUser(User user) {
        if (user != null) {
            getUsers().add(user);
            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }
            user.getRoles().add(this);
        }
    }

    public void removeUser(User user) {
        if (user != null) {
            getUsers().remove(user);
            if (user.getRoles() != null) {
                user.getRoles().remove(this);
            }
        }
    }

    public void addParticipant(Participant participant) {
        if (participant != null) {
            getParticipants().add(participant);
            if (participant.getRoles() == null) {
                participant.setRoles(new HashSet<>());
            }
            participant.getRoles().add(this);
        }
    }

    public void removeParticipant(Participant participant) {
        if (participant != null) {
            getParticipants().remove(participant);
            if (participant.getRoles() != null) {
                participant.getRoles().remove(this);
            }
        }
    }

    public boolean hasUser(User user) {
        return user != null && getUsers().contains(user);
    }

    public boolean hasParticipant(Participant participant) {
        return participant != null && getParticipants().contains(participant);
    }

    @Override
    public String toString() {
        return String.format("Role[id=%d, name=%s, code=%s, priority=%d]", id, name, code, priority);
    }
}
