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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User activeUser;
    private User inactiveUser;
    private User deletedUser;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = roleRepository.save(Role.builder().name(RoleName.USER).build());
        adminRole = roleRepository.save(Role.builder().name(RoleName.ADMIN).build());

        activeUser = User.builder()
                .username("active")
                .password("password")
                .email("active@test.com")
                .status(EntityStatus.ACTIVE)
                .build();
        activeUser.addRole(userRole);

        inactiveUser = User.builder()
                .username("inactive")
                .password("password")
                .email("inactive@test.com")
                .status(EntityStatus.INACTIVE)
                .build();
        inactiveUser.addRole(userRole);

        deletedUser = User.builder()
                .username("deleted")
                .password("password")
                .email("deleted@test.com")
                .status(EntityStatus.DELETED)
                .build();
        deletedUser.addRole(userRole);

        userRepository.saveAll(List.of(activeUser, inactiveUser, deletedUser));
    }

    @Test
    void findByUsername_ShouldReturnUser() {
        assertThat(userRepository.findByUsername("active")).isPresent()
                .hasValueSatisfying(user -> assertThat(user.getUsername()).isEqualTo("active"));
    }

    @Test
    void findAllByStatus_ShouldReturnCorrectUsers() {
        List<User> activeUsers = userRepository.findAllByStatus(EntityStatus.ACTIVE);
        assertThat(activeUsers).hasSize(1)
                .extracting(User::getUsername)
                .containsExactly("active");
    }

    @Test
    void findAllByStatus_WithPagination_ShouldReturnPagedResult() {
        Page<User> activePage = userRepository.findAllByStatus(EntityStatus.ACTIVE, PageRequest.of(0, 10));
        assertThat(activePage.getContent()).hasSize(1)
                .extracting(User::getUsername)
                .containsExactly("active");
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        assertThat(userRepository.countByStatus(EntityStatus.ACTIVE)).isEqualTo(1);
        assertThat(userRepository.countByStatus(EntityStatus.INACTIVE)).isEqualTo(1);
        assertThat(userRepository.countByStatus(EntityStatus.DELETED)).isEqualTo(1);
    }

    @Test
    void updateStatus_ShouldChangeUserStatus() {
        userRepository.updateStatus(activeUser.getId(), EntityStatus.INACTIVE);
        User updatedUser = userRepository.findById(activeUser.getId()).orElseThrow();
        assertThat(updatedUser.getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    void findActiveByRole_ShouldReturnCorrectUsers() {
        List<User> usersWithRole = userRepository.findActiveByRole(RoleName.USER);
        assertThat(usersWithRole).hasSize(1)
                .extracting(User::getUsername)
                .containsExactly("active");
    }

    @Test
    void findActiveByRoles_ShouldReturnCorrectUsers() {
        Set<RoleName> roles = new HashSet<>();
        roles.add(RoleName.USER);
        roles.add(RoleName.ADMIN);

        List<User> usersWithRoles = userRepository.findActiveByRoles(roles);
        assertThat(usersWithRoles).hasSize(1)
                .extracting(User::getUsername)
                .containsExactly("active");
    }

    @Test
    void findActiveByEmail_ShouldReturnCorrectUser() {
        assertThat(userRepository.findActiveByEmail("active@test.com"))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getEmail()).isEqualTo("active@test.com");
                    assertThat(user.isActive()).isTrue();
                });
    }

    @Test
    void existsActiveByUsername_ShouldReturnCorrectResult() {
        assertThat(userRepository.existsActiveByUsername("active")).isTrue();
        assertThat(userRepository.existsActiveByUsername("inactive")).isFalse();
    }

    @Test
    void findAllEnabledAndActive_ShouldReturnCorrectUsers() {
        List<User> enabledAndActive = userRepository.findAllEnabledAndActive();
        assertThat(enabledAndActive).hasSize(1)
                .extracting(User::getUsername)
                .containsExactly("active");
    }

    @Test
    void countEnabledAndActive_ShouldReturnCorrectCount() {
        assertThat(userRepository.countEnabledAndActive()).isEqualTo(1);
    }
}
