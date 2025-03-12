package vn.com.fecredit.app.util;

import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.entity.Role;

/**
 * Constants for commonly used roles in tests
 */
public class TestRoleConstants {
    
    public static Role createAdminRole() {
        Role role = new Role();
        role.setId(1L);
        role.setName(RoleName.ROLE_ADMIN);
        role.setStatus(EntityStatus.ACTIVE);
        return role;
    }

    public static Role createUserRole() {
        Role role = new Role();
        role.setId(2L);
        role.setName(RoleName.ROLE_USER);
        role.setStatus(EntityStatus.ACTIVE);
        return role;
    }

    public static Role createParticipantRole() {
        Role role = new Role();
        role.setId(3L);
        role.setName(RoleName.ROLE_PARTICIPANT);
        role.setStatus(EntityStatus.ACTIVE);
        return role;
    }

    public static Role createPremiumParticipantRole() {
        Role role = new Role();
        role.setId(4L);
        role.setName(RoleName.ROLE_PREMIUM_PARTICIPANT);
        role.setStatus(EntityStatus.ACTIVE);
        return role;
    }

    public static Role createModeratorRole() {
        Role role = new Role();
        role.setId(5L);
        role.setName(RoleName.ROLE_MODERATOR);
        role.setStatus(EntityStatus.ACTIVE);
        return role;
    }

    public static Role createManagerRole() {
        Role role = new Role();
        role.setId(6L);
        role.setName(RoleName.ROLE_MANAGER);
        role.setStatus(EntityStatus.ACTIVE);
        return role;
    }

    public static Role createOperatorRole() {
        Role role = new Role();
        role.setId(7L);
        role.setName(RoleName.ROLE_OPERATOR);
        role.setStatus(EntityStatus.ACTIVE);
        return role;
    }
}
