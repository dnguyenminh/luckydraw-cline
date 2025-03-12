package vn.com.fecredit.app.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import vn.com.fecredit.app.dto.ResetPasswordRequest;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AuthMapperTest {

    private AuthMapper authMapper;

    @BeforeEach
    void setUp() {
        authMapper = new AuthMapper();
    }

    @Test
    void toUserInfo_ShouldMapAllFields() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .fullName("Test User")
                .phoneNumber("1234567890")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(createRoles())
                .build();

        // When
        ResetPasswordRequest.UserInfo userInfo = authMapper.toUserInfo(user);

        // Then
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getId()).isEqualTo(user.getId());
        assertThat(userInfo.getUsername()).isEqualTo(user.getUsername());
        assertThat(userInfo.getEmail()).isEqualTo(user.getEmail());
        assertThat(userInfo.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userInfo.getLastName()).isEqualTo(user.getLastName());
        assertThat(userInfo.getFullName()).isEqualTo(user.getFullName());
        assertThat(userInfo.getPhoneNumber()).isEqualTo(user.getPhoneNumber());
        assertThat(userInfo.isEnabled()).isEqualTo(user.isEnabled());
        assertThat(userInfo.isAccountNonExpired()).isEqualTo(user.isAccountNonExpired());
        assertThat(userInfo.isAccountNonLocked()).isEqualTo(user.isAccountNonLocked());
        assertThat(userInfo.isCredentialsNonExpired()).isEqualTo(user.isCredentialsNonExpired());
        assertThat(userInfo.getAuthorities()).containsExactlyInAnyOrder(
            RoleName.USER.name(),
            RoleName.ADMIN.name()
        );
    }

    @Test
    void toUserInfo_WhenUserIsNull_ShouldReturnNull() {
        assertThat(authMapper.toUserInfo(null)).isNull();
    }

    @Test
    void toUserInfo_WhenUserHasNoRoles_ShouldMapWithEmptyAuthorities() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .roles(new HashSet<>())
                .build();

        // When
        ResetPasswordRequest.UserInfo userInfo = authMapper.toUserInfo(user);

        // Then
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getAuthorities()).isEmpty();
    }

    @Test
    void getAuthorities_ShouldMapRolesToAuthorities() {
        // Given
        Set<Role> roles = createRoles();

        // When
        Collection<? extends GrantedAuthority> authorities = authMapper.getAuthorities(roles);

        // Then
        Set<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertThat(authorityNames).containsExactlyInAnyOrder(
            "ROLE_" + RoleName.USER.name(),
            "ROLE_" + RoleName.ADMIN.name()
        );
    }

    @Test
    void getAuthorities_WhenRolesIsEmpty_ShouldReturnEmptySet() {
        // Given
        Set<Role> roles = new HashSet<>();

        // When
        Collection<? extends GrantedAuthority> authorities = authMapper.getAuthorities(roles);

        // Then
        assertThat(authorities).isEmpty();
    }

    @Test
    void toLoginResponse_ShouldCreateValidResponse() {
        // Given
        String token = "test-token";
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .roles(createRoles())
                .build();

        // When
        ResetPasswordRequest.LoginResponse response = authMapper.toLoginResponse(token, user);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo(user.getUsername());
        assertThat(response.getUser().getAuthorities()).containsExactlyInAnyOrder(
            RoleName.USER.name(),
            RoleName.ADMIN.name()
        );
    }

    private Set<Role> createRoles() {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.builder().name(RoleName.USER).build());
        roles.add(Role.builder().name(RoleName.ADMIN).build());
        return roles;
    }
}
