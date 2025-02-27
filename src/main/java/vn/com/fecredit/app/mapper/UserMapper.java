package vn.com.fecredit.app.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.dto.UserDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;

@Component
@RequiredArgsConstructor
public class UserMapper {

    public User toEntity(UserDTO.CreateRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .active(request.getIsActive())
                .build();
        
        if (request.getRoles() != null) {
            user.setRoles(request.getRoles().stream()
                    .map(roleName -> Role.builder().name(roleName).build())
                    .collect(Collectors.toSet()));
        }
        
        return user;
    }

    public void updateEntity(User user, UserDTO.UpdateRequest request) {
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getIsActive() != null) {
            user.setActive(request.getIsActive());
        }
        if (request.getRoles() != null) {
            user.setRoles(request.getRoles().stream()
                    .map(roleName -> Role.builder().name(roleName).build())
                    .collect(Collectors.toSet()));
        }
    }

    public UserDTO toDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet()))
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .version(user.getVersion())
                .build();
    }

    public UserDTO.UserProfile toUserProfile(User user, Integer participationCount, 
            Integer totalSpins, Integer winningSpins, java.time.LocalDateTime lastActivityDate) {
        return UserDTO.UserProfile.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet()))
                .isActive(user.isActive())
                .participationCount(participationCount)
                .totalSpins(totalSpins)
                .winningSpins(winningSpins)
                .lastActivityDate(lastActivityDate)
                .build();
    }

    public List<UserDTO> toDtoList(List<User> users) {
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Role toRole(RoleName roleName) {
        return Role.builder()
                .name(roleName)
                .build();
    }
}
