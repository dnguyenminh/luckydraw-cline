package vn.com.fecredit.app.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.dto.UserDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserMapperTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleMapper roleMapper;

    private UserMapper mapper;
    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2025, 3, 4, 19, 48, 55);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new UserMapper(passwordEncoder, roleMapper);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        UserDTO.CreateRequest request = UserDTO.CreateRequest.builder()
                .username("test")
                .password("password")
                .email("test@email.com")
                .firstName("First")
                .lastName("Last")
                .phoneNumber("1234567890")
                .status(EntityStatus.ACTIVE)
                .build();

        User user = mapper.toEntity(request);

        assertThat(user).isNotNull()
                .satisfies(u -> {
                    assertThat(u.getUsername()).isEqualTo("test");
                    assertThat(u.getPassword()).isEqualTo("encoded");
                    assertThat(u.getEmail()).isEqualTo("test@email.com");
                    assertThat(u.getFirstName()).isEqualTo("First");
                    assertThat(u.getLastName()).isEqualTo("Last");
                    assertThat(u.getPhoneNumber()).isEqualTo("1234567890");
                    assertThat(u.getStatus()).isEqualTo(EntityStatus.ACTIVE);
                });
    }

    @Test
    void toResponse_ShouldMapAllFields() {
        User user = createTestUser();
        RoleDTO.Summary roleSummary = RoleDTO.Summary.builder()
                .name(RoleName.USER)
                .build();
        when(roleMapper.toSummary(any())).thenReturn(roleSummary);

        UserDTO.Response response = mapper.toResponse(user);

        assertThat(response).isNotNull()
                .satisfies(r -> {
                    assertThat(r.getId()).isEqualTo(1L);
                    assertThat(r.getUsername()).isEqualTo("test");
                    assertThat(r.getEmail()).isEqualTo("test@email.com");
                    assertThat(r.getFirstName()).isEqualTo("First");
                    assertThat(r.getLastName()).isEqualTo("Last");
                    assertThat(r.getPhoneNumber()).isEqualTo("1234567890");
                    assertThat(r.getStatus()).isEqualTo(EntityStatus.ACTIVE);
                    assertThat(r.getLastLogin()).isEqualTo(FIXED_TIME);
                    assertThat(r.isAccountNonLocked()).isTrue();
                    assertThat(r.getRoles()).hasSize(1);
                });
    }

    @Test
    void toSummary_ShouldMapAllFields() {
        User user = createTestUser();
        UserDTO.Summary summary = mapper.toSummary(user);

        assertThat(summary).isNotNull()
                .satisfies(s -> {
                    assertThat(s.getId()).isEqualTo(1L);
                    assertThat(s.getUsername()).isEqualTo("test");
                    assertThat(s.getEmail()).isEqualTo("test@email.com");
                    assertThat(s.getFirstName()).isEqualTo("First");
                    assertThat(s.getLastName()).isEqualTo("Last");
                    assertThat(s.getPhoneNumber()).isEqualTo("1234567890");
                    assertThat(s.getStatus()).isEqualTo(EntityStatus.ACTIVE);
                    assertThat(s.getRoles()).hasSize(1)
                            .contains(RoleName.USER);
                });
    }

    @Test
    void toLoginInfo_ShouldMapAllFields() {
        User user = createTestUser();
        UserDTO.LoginInfo info = mapper.toLoginInfo(user);

        assertThat(info).isNotNull()
                .satisfies(i -> {
                    assertThat(i.getUsername()).isEqualTo("test");
                    assertThat(i.getEmail()).isEqualTo("test@email.com");
                    assertThat(i.getFirstName()).isEqualTo("First");
                    assertThat(i.getLastName()).isEqualTo("Last");
                    assertThat(i.getPhoneNumber()).isEqualTo("1234567890");
                    assertThat(i.getStatus()).isEqualTo(EntityStatus.ACTIVE);
                    assertThat(i.isAccountNonExpired()).isTrue();
                    assertThat(i.isAccountNonLocked()).isTrue();
                    assertThat(i.isCredentialsNonExpired()).isTrue();
                    assertThat(i.getRoles()).hasSize(1)
                            .contains("USER");
                });
    }

    private User createTestUser() {
        Role role = Role.builder()
                .name(RoleName.USER)
                .build();
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .id(1L)
                .username("test")
                .password("encoded")
                .email("test@email.com")
                .firstName("First")
                .lastName("Last")
                .phoneNumber("1234567890")
                .status(EntityStatus.ACTIVE)
                .lastLogin(FIXED_TIME)
                .build();
        user.setRoles(roles);
        return user;
    }
}
