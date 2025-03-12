package vn.com.fecredit.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RoleNotFoundException extends RuntimeException {
    
    private final Long roleId;
    private final String roleName;

    public RoleNotFoundException(String message) {
        super(message);
        this.roleId = null;
        this.roleName = null;
    }

    public RoleNotFoundException(Long id) {
        super("Role not found with id: " + id);
        this.roleId = id;
        this.roleName = null;
    }

    public RoleNotFoundException(String name, String message) {
        super(message);
        this.roleId = null;
        this.roleName = name;
    }

    public RoleNotFoundException(Long id, String message) {
        super(message);
        this.roleId = id;
        this.roleName = null;
    }

    public RoleNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.roleId = null;
        this.roleName = null;
    }

    public static RoleNotFoundException withId(Long id) {
        return new RoleNotFoundException(id);
    }

    public static RoleNotFoundException withName(String name) {
        return new RoleNotFoundException("Role not found with name: " + name);
    }

    public static RoleNotFoundException withCustomMessage(String message) {
        return new RoleNotFoundException(message);
    }

    @Override
    public String getMessage() {
        if (roleId != null) {
            return "Role not found with id: " + roleId;
        }
        if (roleName != null) {
            return "Role not found with name: " + roleName;
        }
        return super.getMessage();
    }
}
