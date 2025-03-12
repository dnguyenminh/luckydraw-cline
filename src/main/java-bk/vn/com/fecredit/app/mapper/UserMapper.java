package vn.com.fecredit.app.mapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.dto.UserDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;
    private final RoleMapper roleMapper;

    public UserMapper(PasswordEncoder passwordEncoder, RoleMapper roleMapper) {
        this.passwordEncoder = passwordEncoder;
        this.roleMapper = roleMapper;
    }

    public User toEntity(UserDTO.CreateRequest request) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .status(request.getStatus())
                .build();
    }

    public void updateEntity(User user, UserDTO.UpdateRequest request) {
        if (user == null || request == null) {
            return;
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
    }

    public UserDTO.Response toResponse(User user) {
        if (user == null) {
            return null;
        }

        Set<RoleDTO.Summary> roleSummaries = user.getRoles().stream()
                .map(roleMapper::toSummary)
                .collect(Collectors.toSet());

        return UserDTO.Response.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .lastLogin(user.getLastLogin())
                .accountNonLocked(!user.isAccountLocked())
                .credentialsNonExpired(!user.isPasswordExpired())
                .roles(roleSummaries)
                .createdBy(user.getCreatedBy())
                .createdAt(user.getCreatedAt())
                .updatedBy(user.getUpdatedBy())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserDTO.Summary toSummary(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.Summary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();
    }

    public UserDTO.LoginInfo toLoginInfo(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.LoginInfo.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .lastLogin(user.getLastLogin())
                .failedAttempts(user.getFailedAttempts())
                .lockedUntil(user.getLockedUntil())
                .build();
    }

    public List<UserDTO.Response> toResponseList(List<User> users) {
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<UserDTO.Summary> toSummaryList(List<User> users) {
        return users.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public Set<UserDTO.Response> toResponseSet(Set<User> users) {
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }

    public Set<UserDTO.Summary> toSummarySet(Set<User> users) {
        return users.stream()
                .map(this::toSummary)
                .collect(Collectors.toSet());
    }
}
