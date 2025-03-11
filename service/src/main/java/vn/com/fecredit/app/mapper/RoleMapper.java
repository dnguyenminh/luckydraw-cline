package vn.com.fecredit.app.mapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.Permission;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {UserMapper.class}
)
public interface RoleMapper {

    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "users", ignore = true)
    Role toEntity(RoleDTO.CreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "permissions", ignore = true)
    void updateEntity(@MappingTarget Role entity, RoleDTO.UpdateRequest request);

    @Mapping(target = "permissions", expression = "java(getPermissionStrings(entity))")
    RoleDTO.Response toResponse(Role entity);

    @Mapping(target = "permissions", expression = "java(getPermissionStrings(entity))")
    RoleDTO.Summary toSummary(Role entity);

    List<RoleDTO.Response> toResponseList(List<Role> entities);

    List<RoleDTO.Summary> toSummaryList(List<Role> entities);

    default Page<RoleDTO.Summary> toSummaryPage(Page<Role> page) {
        return page.map(this::toSummary);
    }

    default Set<String> getPermissionStrings(Role role) {
        if (role == null || role.getPermissions() == null) {
            return new HashSet<>();
        }
        return role.getPermissions().stream()
                .map(Permission::name)
                .collect(Collectors.toSet());
    }

    @AfterMapping
    default void setPermissions(RoleDTO.CreateRequest request, @MappingTarget Role entity) {
        if (request.getPermissions() != null) {
            entity.setPermissions(request.getPermissions());
        }
    }

    @AfterMapping
    default void setPermissionsForUpdate(RoleDTO.UpdateRequest request, @MappingTarget Role entity) {
        if (request.getPermissions() != null) {
            entity.setPermissions(request.getPermissions());
        }
    }

    default void handleRoleAssignment(Role role, User user) {
        if (role != null && user != null) {
            role.addUser(user);
        }
    }

    default void handleRoleUnassignment(Role role, User user) {
        if (role != null && user != null) {
            role.removeUser(user);
        }
    }

    default void updateFromPermissionUpdate(Role role, RoleDTO.PermissionUpdate request) {
        if (request != null && request.getPermissions() != null) {
            role.setPermissions(request.getPermissions());
        }
    }
}
