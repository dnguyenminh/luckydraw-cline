package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    private Role adminRole;
    private Role userRole;
    private Role managerRole;
    private User activeUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        // Create roles
        adminRole = Role.builder().name(RoleName.ADMIN).description("Administrator").build();
        userRole = Role.builder().name(RoleName.USER).description("Regular User").build();
        managerRole = Role.builder().name(RoleName.MANAGER).description("Manager").build();

        adminRole.setStatus(EntityStatus.ACTIVE);
        userRole.setStatus(EntityStatus.ACTIVE);
        managerRole.setStatus(EntityStatus.INACTIVE);

        roleRepository.saveAll(List.of(adminRole, userRole, managerRole));

        // Create users
        activeUser = User.builder()
                .username("active")
                .password("password")
                .enabled(true)
                .build();
        activeUser.setStatus(EntityStatus.ACTIVE);
        activeUser.addRole(adminRole);
        activeUser.addRole(userRole);

        inactiveUser = User.builder()
                .username("inactive")
                .password("password")
                .enabled(false)
                .build();
        inactiveUser.setStatus(EntityStatus.INACTIVE);
        inactiveUser.addRole(userRole);

        userRepository.saveAll(List.of(activeUser, inactiveUser));
    }

    @Test
    void findByName_ShouldReturnRole() {
        assertThat(roleRepository.findByName(RoleName.ADMIN))
                .isPresent()
                .hasValueSatisfying(role -> {
                    assertThat(role.getName()).isEqualTo(RoleName.ADMIN);
                    assertThat(role.getDescription()).isEqualTo("Administrator");
                });
    }

    @Test
    void findByNameIn_ShouldReturnRoles() {
        Set<Role> roles = roleRepository.findByNameIn(Set.of(RoleName.ADMIN, RoleName.USER));
        assertThat(roles).hasSize(2)
                .extracting(Role::getName)
                .containsExactlyInAnyOrder(RoleName.ADMIN, RoleName.USER);
    }

    @Test
    void findAllByStatus_ShouldReturnCorrectRoles() {
        List<Role> activeRoles = roleRepository.findAllByStatus(EntityStatus.ACTIVE);
        assertThat(activeRoles).hasSize(2)
                .extracting(Role::getName)
                .containsExactlyInAnyOrder(RoleName.ADMIN, RoleName.USER);

        List<Role> inactiveRoles = roleRepository.findAllByStatus(EntityStatus.INACTIVE);
        assertThat(inactiveRoles).hasSize(1)
                .extracting(Role::getName)
                .containsExactly(RoleName.MANAGER);
    }

    @Test
    void findAllByStatus_WithPagination_ShouldReturnPagedResult() {
        Page<Role> activePage = roleRepository.findAllByStatus(EntityStatus.ACTIVE, PageRequest.of(0, 10));
        assertThat(activePage.getContent()).hasSize(2)
                .extracting(Role::getName)
                .containsExactlyInAnyOrder(RoleName.ADMIN, RoleName.USER);
    }

    @Test
    void findActiveByName_ShouldReturnActiveRole() {
        assertThat(roleRepository.findActiveByName(RoleName.ADMIN)).isPresent();
        assertThat(roleRepository.findActiveByName(RoleName.MANAGER)).isEmpty();
    }

    @Test
    void findActiveByNameIn_ShouldReturnActiveRoles() {
        Set<Role> activeRoles = roleRepository.findActiveByNameIn(
                Set.of(RoleName.ADMIN, RoleName.USER, RoleName.MANAGER));
        assertThat(activeRoles).hasSize(2)
                .extracting(Role::getName)
                .containsExactlyInAnyOrder(RoleName.ADMIN, RoleName.USER);
    }

    @Test
    void existsActiveByName_ShouldReturnCorrectResult() {
        assertThat(roleRepository.existsActiveByName(RoleName.ADMIN)).isTrue();
        assertThat(roleRepository.existsActiveByName(RoleName.MANAGER)).isFalse();
    }

    @Test
    void findActiveByUserId_ShouldReturnUserRoles() {
        Set<Role> activeUserRoles = roleRepository.findActiveByUserId(activeUser.getId());
        assertThat(activeUserRoles).hasSize(2)
                .extracting(Role::getName)
                .containsExactlyInAnyOrder(RoleName.ADMIN, RoleName.USER);
    }

    @Test
    void hasUserRole_ShouldCheckRoleCorrectly() {
        assertThat(roleRepository.hasUserRole(activeUser.getId(), RoleName.ADMIN)).isTrue();
        assertThat(roleRepository.hasUserRole(inactiveUser.getId(), RoleName.ADMIN)).isFalse();
    }

    @Test
    void getUserRoleNames_ShouldReturnRoleNames() {
        Set<RoleName> roleNames = roleRepository.getUserRoleNames(activeUser.getId());
        assertThat(roleNames).hasSize(2)
                .containsExactlyInAnyOrder(RoleName.ADMIN, RoleName.USER);
    }

    @Test
    void countUsersByRoleId_ShouldReturnCorrectCount() {
        assertThat(roleRepository.countUsersByRoleId(userRole.getId())).isEqualTo(2);
        assertThat(roleRepository.countUsersByRoleId(adminRole.getId())).isEqualTo(1);
    }
}
