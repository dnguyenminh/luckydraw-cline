package vn.com.fecredit.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RoleOperationException extends RuntimeException {
    
    private final Long roleId;
    private final String operation;
    private final String reason;

    public RoleOperationException(String message) {
        super(message);
        this.roleId = null;
        this.operation = null;
        this.reason = null;
    }

    public RoleOperationException(Long roleId, String operation, String reason) {
        super(String.format("Failed to %s role with ID %d: %s", operation, roleId, reason));
        this.roleId = roleId;
        this.operation = operation;
        this.reason = reason;
    }

    public RoleOperationException(String message, Throwable cause) {
        super(message, cause);
        this.roleId = null;
        this.operation = null;
        this.reason = cause.getMessage();
    }

    public static RoleOperationException createFailed(String reason) {
        return new RoleOperationException("Failed to create role: " + reason);
    }

    public static RoleOperationException updateFailed(Long roleId, String reason) {
        return new RoleOperationException(roleId, "update", reason);
    }

    public static RoleOperationException deleteFailed(Long roleId, String reason) {
        return new RoleOperationException(roleId, "delete", reason);
    }

    public static RoleOperationException assignmentFailed(Long roleId, Long userId, String reason) {
        return new RoleOperationException(
            String.format("Failed to assign role %d to user %d: %s", roleId, userId, reason)
        );
    }

    public static RoleOperationException unassignmentFailed(Long roleId, Long userId, String reason) {
        return new RoleOperationException(
            String.format("Failed to unassign role %d from user %d: %s", roleId, userId, reason)
        );
    }

    public static RoleOperationException permissionUpdateFailed(Long roleId, String reason) {
        return new RoleOperationException(roleId, "update permissions", reason);
    }

    public static RoleOperationException statusUpdateFailed(Long roleId, String status, String reason) {
        return new RoleOperationException(
            String.format("Failed to update role %d status to %s: %s", roleId, status, reason)
        );
    }
}
