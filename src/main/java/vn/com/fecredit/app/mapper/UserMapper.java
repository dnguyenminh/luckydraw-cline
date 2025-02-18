package vn.com.fecredit.app.mapper;

import org.springframework.stereotype.Component;
import vn.com.fecredit.app.dto.UserDto;
import vn.com.fecredit.app.dto.UserInfoDto;
import vn.com.fecredit.app.model.Role;
import vn.com.fecredit.app.model.User;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for User entities and DTOs.
 */
@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .roles(rolesToStringSet(user.getRoles()))
            .phoneNumber(user.getPhoneNumber())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    public Set<String> rolesToStringSet(Set<Role> roles) {
        if (roles == null) {
            return new HashSet<>();
        }
        return roles.stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
    }

    public void updateFromUserInfoDto(UserInfoDto userInfoDto, User user) {
        if (userInfoDto == null || user == null) {
            return;
        }
        if (userInfoDto.getFirstName() != null) {
            user.setFirstName(userInfoDto.getFirstName());
        }
        if (userInfoDto.getLastName() != null) {
            user.setLastName(userInfoDto.getLastName());
        }
    }
}