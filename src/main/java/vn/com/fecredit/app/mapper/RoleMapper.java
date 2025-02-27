package vn.com.fecredit.app.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.enums.RoleName;

@Component
@RequiredArgsConstructor
public class RoleMapper {

    public Role toEntity(RoleDTO.CreateRequest request) {
        return Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    public void updateEntity(Role role, RoleDTO.UpdateRequest request) {
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
    }

    public RoleDTO toDto(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .userCount(role.getUsers().size())
                .version(role.getVersion())
                .build();
    }

    public RoleDTO.Response toResponse(Role role) {
        return RoleDTO.Response.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .userCount(role.getUsers().size())
                .version(role.getVersion())
                .build();
    }

    public RoleDTO.Summary toSummary(Role role) {
        return RoleDTO.Summary.builder()
                .id(role.getId())
                .name(role.getName())
                .userCount(role.getUsers().size())
                .build();
    }

    public List<RoleDTO> toDtoList(List<Role> roles) {
        return roles.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<RoleDTO.Response> toResponseList(List<Role> roles) {
        return roles.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<RoleDTO.Summary> toSummaryList(List<Role> roles) {
        return roles.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public Set<Role> toEntitySet(Set<RoleName> roleNames) {
        return roleNames.stream()
                .map(Role::from)
                .collect(Collectors.toSet());
    }

    public Set<RoleName> toRoleNameSet(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    public RoleDTO.Privileges toPrivileges(Role role, Set<String> permissions, Integer level, Boolean isSystem) {
        return RoleDTO.Privileges.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(permissions)
                .level(level)
                .isSystem(isSystem)
                .build();
    }
}
