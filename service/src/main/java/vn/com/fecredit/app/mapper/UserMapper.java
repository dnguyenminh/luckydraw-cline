package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import vn.com.fecredit.app.dto.UserDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.enums.RoleName;
import vn.com.fecredit.app.dto.RoleDTO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {RoleMapper.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "roles", expression = "java(roleNamesToRoles(dto.getRoles()))")
    User toEntity(UserDTO.CreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget User target, UserDTO.UpdateRequest source);

    @Mapping(target = "roles", expression = "java(rolesToSummaries(user.getRoles()))")
    UserDTO.Response toResponse(User user);

    List<UserDTO.Response> toResponseList(List<User> users);

    @Mapping(target = "roles", expression = "java(rolesToSummaries(user.getRoles()))")
    UserDTO.Summary toSummary(User user);

    List<UserDTO.Summary> toSummaryList(List<User> users);

    @Mapping(target = "roles", expression = "java(summariesToRoles(response.getRoles()))")
    User fromResponse(UserDTO.Response response);

    @Mapping(target = "roles", expression = "java(response.getRoles())")
    UserDTO.Summary responseToSummary(UserDTO.Response response);

    default Set<Role> roleNamesToRoles(Set<RoleName> roleNames) {
        if (roleNames == null) return null;
        return roleNames.stream()
                .map(name -> Role.builder().name(name).build())
                .collect(Collectors.toSet());
    }

    default Set<RoleDTO.Summary> rolesToSummaries(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(role -> RoleDTO.Summary.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .code(role.getCode())
                        .description(role.getDescription())
                        .status(role.getStatus())
                        .build())
                .collect(Collectors.toSet());
    }

    default Set<Role> summariesToRoles(Set<RoleDTO.Summary> summaries) {
        if (summaries == null) return null;
        return summaries.stream()
                .map(summary -> Role.builder()
                        .id(summary.getId())
                        .name(summary.getName())
                        .code(summary.getCode())
                        .description(summary.getDescription())
                        .status(summary.getStatus())
                        .build())
                .collect(Collectors.toSet());
    }

    @AfterMapping
    default void handleNullCollections(@MappingTarget User user) {
        if (user.getRoles() == null) {
            user.setRoles(Set.of());
        }
    }

    @AfterMapping
    default void updateRoles(@MappingTarget User target, UserDTO.UpdateRequest source) {
        if (source.getRoles() != null) {
            target.setRoles(roleNamesToRoles(source.getRoles()));
        }
    }
}
