package vn.com.fecredit.app.model;

public enum RoleName {
    ROLE_ADMIN,
    ROLE_USER,
    ROLE_MANAGER,
    ROLE_STAFF;

    public String getValue() {
        return this.name();
    }

    public static RoleName fromValue(String value) {
        for (RoleName role : RoleName.values()) {
            if (role.name().equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role name: " + value);
    }
}
