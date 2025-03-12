package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.entity.Role;

import java.util.List;
import java.util.Set;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Role toEntity(RoleDTO.CreateRequest dto);

    @Mapping(target = "userCount", expression = "java(role.getUsers() != null ? role.getUsers().size() : 0)")
    RoleDTO.Response toResponse(Role role);

    List<RoleDTO.Response> toResponseList(List<Role> roles);

    @Mapping(target = "displayName", expression = "java(role.getName().getDisplayName())")
    @Mapping(target = "userCount", expression = "java(role.getUsers() != null ? role.getUsers().size() : 0)")
    RoleDTO.Summary toSummary(Role role);

    List<RoleDTO.Summary> toSummaryList(List<Role> roles);

    Set<RoleDTO.Summary> toSummarySet(Set<Role> roles);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget Role role, RoleDTO.UpdateRequest dto);

    @AfterMapping
    default void setStatus(@MappingTarget Role role, RoleDTO.UpdateRequest dto) {
        if (dto.getActive() != null) {
            role.setStatus(dto.getActive() ? EntityStatus.ACTIVE : EntityStatus.INACTIVE);
        }
    }

    default String mapRoleName(Role role) {
        return role.getName().name();
    }

    default String mapRoleDisplayName(Role role) {
        return role.getName().getDisplayName();
    }

    @Named("toRoleSummary")
    @Mapping(target = "displayName", expression = "java(role.getName().getDisplayName())")
    @Mapping(target = "userCount", expression = "java(role.getUsers() != null ? role.getUsers().size() : 0)")
    RoleDTO.Summary toRoleSummary(Role role);

    @IterableMapping(qualifiedByName = "toRoleSummary")
    Set<RoleDTO.Summary> toRoleSummaries(Set<Role> roles);
}
