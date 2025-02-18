import {
  UserRole,
  Permission,
  ROLE_HIERARCHY,
  ROLE_LABELS,
  ROLE_PERMISSIONS,
  hasRole,
  hasPermission,
  hasAnyPermission,
  hasAllPermissions,
  getHighestRole,
  getRoleLabel,
  getAllPermissionsForRoles
} from './roles';

describe('Role Constants and Utilities', () => {
  describe('Role Hierarchy', () => {
    it('should maintain correct role hierarchy order', () => {
      expect(ROLE_HIERARCHY).toEqual([
        UserRole.SUPER_ADMIN,
        UserRole.ADMIN,
        UserRole.MANAGER,
        UserRole.USER,
        UserRole.GUEST
      ]);
    });

    it('should have a label for each role', () => {
      Object.values(UserRole).forEach(role => {
        expect(ROLE_LABELS[role]).toBeDefined();
        expect(typeof ROLE_LABELS[role]).toBe('string');
      });
    });
  });

  describe('Role Permissions', () => {
    it('should define permissions for each role', () => {
      Object.values(UserRole).forEach(role => {
        expect(ROLE_PERMISSIONS[role]).toBeDefined();
        expect(Array.isArray(ROLE_PERMISSIONS[role])).toBe(true);
      });
    });

    it('should give SUPER_ADMIN all permissions', () => {
      const superAdminPermissions = ROLE_PERMISSIONS[UserRole.SUPER_ADMIN];
      const allPermissions = Object.values(Permission);
      
      // Check that all permissions are included
      allPermissions.forEach(permission => {
        expect(superAdminPermissions).toContain(permission);
      });
      
      // Check that there are no extra permissions
      expect(superAdminPermissions.length).toBe(allPermissions.length);
    });

    it('should give GUEST minimal permissions', () => {
      const guestPermissions = ROLE_PERMISSIONS[UserRole.GUEST];
      expect(guestPermissions).toContain(Permission.VIEW_CONTENT);
      expect(guestPermissions.length).toBeLessThan(ROLE_PERMISSIONS[UserRole.USER].length);
    });
  });

  describe('hasRole', () => {
    it('should return true for exact role match', () => {
      expect(hasRole([UserRole.ADMIN], UserRole.ADMIN)).toBe(true);
    });

    it('should return true for higher role checking lower role', () => {
      expect(hasRole([UserRole.SUPER_ADMIN], UserRole.ADMIN)).toBe(true);
      expect(hasRole([UserRole.ADMIN], UserRole.USER)).toBe(true);
    });

    it('should return false for lower role checking higher role', () => {
      expect(hasRole([UserRole.USER], UserRole.ADMIN)).toBe(false);
      expect(hasRole([UserRole.GUEST], UserRole.USER)).toBe(false);
    });

    it('should handle multiple roles', () => {
      expect(hasRole([UserRole.USER, UserRole.MANAGER], UserRole.USER)).toBe(true);
      expect(hasRole([UserRole.USER, UserRole.GUEST], UserRole.ADMIN)).toBe(false);
    });
  });

  describe('Permission Checks', () => {
    it('should check single permission', () => {
      expect(hasPermission([UserRole.ADMIN], Permission.CREATE_USER)).toBe(true);
      expect(hasPermission([UserRole.USER], Permission.CREATE_USER)).toBe(false);
    });

    it('should check any permission', () => {
      const permissions = [Permission.VIEW_CONTENT, Permission.CREATE_CONTENT];
      expect(hasAnyPermission([UserRole.USER], permissions)).toBe(true);
      expect(hasAnyPermission([UserRole.GUEST], [Permission.CREATE_USER])).toBe(false);
    });

    it('should check all permissions', () => {
      const permissions = [Permission.VIEW_CONTENT, Permission.CREATE_CONTENT];
      expect(hasAllPermissions([UserRole.USER], permissions)).toBe(true);
      expect(hasAllPermissions([UserRole.GUEST], permissions)).toBe(false);
    });
  });

  describe('Utility Functions', () => {
    it('should get highest role', () => {
      expect(getHighestRole([UserRole.USER, UserRole.ADMIN])).toBe(UserRole.ADMIN);
      expect(getHighestRole([UserRole.USER, UserRole.GUEST])).toBe(UserRole.USER);
    });

    it('should get role label', () => {
      expect(getRoleLabel(UserRole.ADMIN)).toBe(ROLE_LABELS[UserRole.ADMIN]);
      expect(getRoleLabel(UserRole.USER)).toBe(ROLE_LABELS[UserRole.USER]);
    });

    it('should get all permissions for roles', () => {
      const userAndManagerRoles = [UserRole.USER, UserRole.MANAGER];
      const allPermissions = getAllPermissionsForRoles(userAndManagerRoles);
      
      // Should include all permissions from both roles
      ROLE_PERMISSIONS[UserRole.USER].forEach(permission => {
        expect(allPermissions).toContain(permission);
      });
      ROLE_PERMISSIONS[UserRole.MANAGER].forEach(permission => {
        expect(allPermissions).toContain(permission);
      });

      // Should not have duplicates
      const uniquePermissions = new Set(allPermissions);
      expect(allPermissions.length).toBe(uniquePermissions.size);

      // Should not include permissions from other roles
      const adminOnlyPermission = Permission.ASSIGN_ROLES;
      expect(allPermissions).not.toContain(adminOnlyPermission);
    });
  });
});