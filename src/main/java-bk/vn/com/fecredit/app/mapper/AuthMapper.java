package vn.com.fecredit.app.mapper;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import vn.com.fecredit.app.dto.ResetPasswordRequest;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthMapper {

    public ResetPasswordRequest.UserInfo toUserInfo(User user) {
        if (user == null) {
            return null;
        }

        Set<String> authorities = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.toSet());

        return ResetPasswordRequest.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .enabled(user.isEnabled())
                .active(user.isActive())
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .authorities(authorities)
                .build();
    }

    public Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toSet());
    }

    public ResetPasswordRequest.LoginResponse toLoginResponse(String token, User user) {
        return ResetPasswordRequest.LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .user(toUserInfo(user))
                .build();
    }
}
