package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.fecredit.app.dto.RoleDTO;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.RoleName;
import vn.com.fecredit.app.service.common.BaseService;

import java.util.Optional;
import java.util.Set;

public interface RoleService extends BaseService<Role, Long, RoleDTO.Response, RoleDTO.CreateRequest, RoleDTO.UpdateRequest> {

    /**
     * Find role by name
     */
    Optional<Role> findByName(RoleName name);

    /**
     * Find active roles by names
     */
    Set<Role> findByNames(Set<RoleName> names);

    /**
     * Get all active roles
     */
    Set<Role> findAllActive();
    
    /**
     * Get page of roles with summaries
     */
    Page<RoleDTO.Summary> findAllSummaries(Pageable pageable);

    /**
     * Check if role exists
     */
    boolean existsByName(RoleName name);

    /**
     * Check if role exists and is active
     */
    boolean existsActiveByName(RoleName name);

    /**
     * Find roles for a participant
     */
    Set<Role> findByParticipantId(Long participantId);

    /**
     * Find roles for a user
     */
    Set<Role> findByUserId(Long userId);

    /**
     * Check if participant has role
     */
    boolean hasParticipantRole(Long participantId, RoleName roleName);

    /**
     * Check if user has role
     */
    boolean hasUserRole(Long userId, RoleName roleName);

    /**
     * Assign roles to participant
     */
    void assignRolesToParticipant(Long participantId, Set<RoleName> roleNames);

    /**
     * Remove roles from participant
     */
    void removeRolesFromParticipant(Long participantId, Set<RoleName> roleNames);

    /**
     * Assign roles to user
     */
    void assignRolesToUser(Long userId, Set<RoleName> roleNames);

    /**
     * Remove roles from user
     */
    void removeRolesFromUser(Long userId, Set<RoleName> roleNames);

    /**
     * Get role assignments for multiple participants
     */
    Set<RoleDTO.Summary> getParticipantRoles(Set<Long> participantIds);

    /**
     * Get role assignments for multiple users
     */
    Set<RoleDTO.Summary> getUserRoles(Set<Long> userIds);

    /**
     * Get highest role for participant
     */
    Optional<Role> getHighestParticipantRole(Long participantId);

    /**
     * Get highest role for user
     */
    Optional<Role> getHighestUserRole(Long userId);
}
