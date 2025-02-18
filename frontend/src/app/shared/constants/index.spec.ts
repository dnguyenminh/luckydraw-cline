import {
  HTTP_STATUS,
  API_CONFIG,
  PAGINATION,
  FILE_UPLOAD,
  NOTIFICATION,
  STORAGE_KEYS,
  VALIDATION,
  ERROR_MESSAGES,
  THEME,
  ROUTES,
  EVENTS,
  DATE_FORMATS
} from './index';

describe('Constants', () => {
  describe('HTTP_STATUS', () => {
    it('should have correct HTTP status codes', () => {
      expect(HTTP_STATUS.OK).toBe(200);
      expect(HTTP_STATUS.CREATED).toBe(201);
      expect(HTTP_STATUS.BAD_REQUEST).toBe(400);
      expect(HTTP_STATUS.UNAUTHORIZED).toBe(401);
      expect(HTTP_STATUS.FORBIDDEN).toBe(403);
      expect(HTTP_STATUS.NOT_FOUND).toBe(404);
      expect(HTTP_STATUS.SERVER_ERROR).toBe(500);
    });
  });

  describe('API_CONFIG', () => {
    it('should have required API configuration', () => {
      expect(API_CONFIG.BASE_URL).toBe('/api');
      expect(API_CONFIG.VERSION).toBe('v1');
      expect(API_CONFIG.DEFAULT_PAGE_SIZE).toBe(10);
    });
  });

  describe('PAGINATION', () => {
    it('should have correct pagination defaults', () => {
      expect(PAGINATION.DEFAULT_PAGE).toBe(0);
      expect(PAGINATION.DEFAULT_SIZE).toBe(API_CONFIG.DEFAULT_PAGE_SIZE);
      expect(PAGINATION.PAGE_SIZES).toContain(10);
    });
  });

  describe('FILE_UPLOAD', () => {
    it('should have correct file upload configuration', () => {
      expect(FILE_UPLOAD.MAX_SIZE).toBeDefined();
      expect(FILE_UPLOAD.IMAGE_TYPES).toEqual(FILE_UPLOAD.ACCEPTED_TYPES.IMAGES);
      expect(FILE_UPLOAD.DOCUMENT_TYPES).toEqual(FILE_UPLOAD.ACCEPTED_TYPES.DOCUMENTS);
    });
  });

  describe('NOTIFICATION', () => {
    it('should have correct notification settings', () => {
      expect(NOTIFICATION.DURATION).toBe(5000);
      expect(NOTIFICATION.POSITION).toBe('top-right');
      expect(NOTIFICATION.MAX_STACK).toBe(5);
      expect(NOTIFICATION.TYPES).toContain('success');
    });
  });

  describe('STORAGE_KEYS', () => {
    it('should have required storage keys', () => {
      expect(STORAGE_KEYS.AUTH_TOKEN).toBeDefined();
      expect(STORAGE_KEYS.USER_INFO).toBeDefined();
      expect(STORAGE_KEYS.THEME).toBeDefined();
      expect(STORAGE_KEYS.SETTINGS).toBeDefined();
    });
  });

  describe('VALIDATION', () => {
    it('should have correct validation messages and rules', () => {
      expect(VALIDATION.REQUIRED).toBeDefined();
      expect(VALIDATION.EMAIL).toBeDefined();
      expect(VALIDATION.MIN_LENGTH(3)).toContain('3');
      expect(VALIDATION.MIN_PASSWORD_LENGTH).toBe(8);
      expect(VALIDATION.MAX_PASSWORD_LENGTH).toBe(32);
      expect(VALIDATION.EMAIL_PATTERN).toBeDefined();
      expect(VALIDATION.PHONE_PATTERN).toBeDefined();
    });
  });

  describe('ERROR_MESSAGES', () => {
    it('should have required error messages', () => {
      expect(ERROR_MESSAGES.DEFAULT).toBeDefined();
      expect(ERROR_MESSAGES.NETWORK).toBeDefined();
      expect(ERROR_MESSAGES.UNAUTHORIZED).toBeDefined();
    });
  });

  describe('THEME', () => {
    it('should have correct theme configuration', () => {
      expect(THEME.LIGHT).toBe('light');
      expect(THEME.DARK).toBe('dark');
      expect(THEME.SYSTEM).toBe('system');
      expect(THEME.COLORS.PRIMARY).toBeDefined();
    });
  });

  describe('ROUTES', () => {
    it('should have correct route definitions', () => {
      expect(ROUTES.HOME).toBe('/');
      expect(ROUTES.AUTH.LOGIN).toBe('/auth/login');
      expect(ROUTES.AUTH.REGISTER).toBe('/auth/register');
      expect(ROUTES.ADMIN.DASHBOARD).toBe('/admin');
    });
  });

  describe('EVENTS', () => {
    it('should have correct event definitions', () => {
      expect(EVENTS.AUTH.LOGIN).toBe('auth:login');
      expect(EVENTS.THEME.CHANGE).toBe('theme:change');
      expect(EVENTS.NOTIFICATION.SHOW).toBe('notification:show');
    });
  });

  describe('DATE_FORMATS', () => {
    it('should have required date formats', () => {
      expect(DATE_FORMATS.SHORT_DATE).toBeDefined();
      expect(DATE_FORMATS.ISO).toBe('yyyy-MM-dd');
      expect(DATE_FORMATS.ISO_WITH_TIME).toBeDefined();
    });
  });
});