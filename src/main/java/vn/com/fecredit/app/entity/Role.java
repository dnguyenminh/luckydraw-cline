package vn.com.fecredit.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.enums.RoleName;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 20, unique = true)
    private RoleName name;

    @Column(length = 200)
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return name != null && name.equals(role.getName());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public String getNameString() {
        return name != null ? name.name() : null;
    }

    public void setNameString(String nameString) {
        if (nameString != null) {
            try {
                this.name = RoleName.valueOf(nameString);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role name: " + nameString);
            }
        }
    }

    public static Role from(RoleName roleName) {
        return Role.builder()
                .name(roleName)
                .build();
    }
}
