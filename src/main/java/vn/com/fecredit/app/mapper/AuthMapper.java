package vn.com.fecredit.app.mapper;

import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.dto.AuthDTO;
import vn.com.fecredit.app.dto.UserDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;

@Component
@RequiredArgsConstructor
public class AuthMapper {

    private final RoleMapper roleMapper;
    
    public UserDTO.UserAuth toUserAuth(User user, String token, java.time.LocalDateTime expiration) {
        return UserDTO.UserAuth.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roleMapper.toRoleNameSet(user.getRoles()))
                .token(token)
                .tokenExpiration(expiration)
                .build();
    }

    public AuthDTO.LoginResponse toLoginResponse(UserDTO.UserAuth userAuth) {
        return AuthDTO.LoginResponse.builder()
                .userId(userAuth.getId())
                .username(userAuth.getUsername())
                .fullName(userAuth.getFullName())
                .roles(userAuth.getRoles())
                .accessToken(userAuth.getToken())
                .tokenExpiration(userAuth.getTokenExpiration())
                .build();
    }

    public User toEntity(AuthDTO.RegisterRequest request) {
        return User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .active(true)
                .build();
    }

    public UserDetails toUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.isActive(),
            true, // account non-expired
            true, // credentials non-expired
            true, // account non-locked
            user.getRoles().stream()
                .map(Role::getName)
                .map(RoleName::name)
                .map(name -> new SimpleGrantedAuthority("ROLE_" + name))
                .collect(Collectors.toSet())
        );
    }

    public AuthDTO.TokenValidationResponse toTokenValidationResponse(
            String username, 
            java.util.Set<? extends GrantedAuthority> authorities, 
            java.time.LocalDateTime expiration) {
        return AuthDTO.TokenValidationResponse.builder()
                .isValid(true)
                .username(username)
                .roles(authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(auth -> auth.replace("ROLE_", ""))
                        .map(RoleName::valueOf)
                        .collect(Collectors.toSet()))
                .expirationTime(expiration)
                .build();
    }

    public AuthDTO.TokenRefreshResponse toTokenRefreshResponse(
            String accessToken, 
            String refreshToken,
            java.time.LocalDateTime expiration) {
        return AuthDTO.TokenRefreshResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenExpiration(expiration)
                .build();
    }
}
