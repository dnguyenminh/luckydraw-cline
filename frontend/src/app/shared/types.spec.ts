import {
  Dict,
  List,
  Tuple,
  ComponentSize,
  ComponentTheme,
  AlertType,
  ModalSize,
  SortDirection,
  ApiResponse,
  PaginatedResponse,
  FileInfo,
  ThemeConfig,
  FormField,
  ErrorState
} from './types';

describe('Types', () => {
  describe('Component Types', () => {
    it('should have correct ComponentSize values', () => {
      const sizes: ComponentSize[] = ['xs', 'sm', 'md', 'lg', 'xl'];
      sizes.forEach(size => {
        const isValid = ['xs', 'sm', 'md', 'lg', 'xl'].includes(size);
        expect(isValid).toBe(true);
      });
    });

    it('should have correct ComponentTheme values', () => {
      const themes: ComponentTheme[] = [
        'primary',
        'secondary',
        'success',
        'danger',
        'warning',
        'info',
        'light',
        'dark',
        'system'
      ];
      themes.forEach(theme => {
        const isValid = [
          'primary',
          'secondary',
          'success',
          'danger',
          'warning',
          'info',
          'light',
          'dark',
          'system'
        ].includes(theme);
        expect(isValid).toBe(true);
      });
    });

    it('should have correct AlertType values', () => {
      const types: AlertType[] = ['success', 'error', 'warning', 'info'];
      types.forEach(type => {
        const isValid = ['success', 'error', 'warning', 'info'].includes(type);
        expect(isValid).toBe(true);
      });
    });

    it('should have correct ModalSize values', () => {
      const sizes: ModalSize[] = ['sm', 'md', 'lg', 'xl', 'full'];
      sizes.forEach(size => {
        const isValid = ['sm', 'md', 'lg', 'xl', 'full'].includes(size);
        expect(isValid).toBe(true);
      });
    });
  });

  describe('API Types', () => {
    it('should create valid ApiResponse', () => {
      const response: ApiResponse<string> = {
        success: true,
        data: 'test',
        meta: {
          timestamp: new Date().toISOString(),
          requestId: '123'
        }
      };
      expect(response.success).toBe(true);
      expect(response.data).toBe('test');
      expect(typeof response.meta.timestamp).toBe('string');
    });

    it('should create valid PaginatedResponse', () => {
      const response: PaginatedResponse<string> = {
        success: true,
        data: ['test1', 'test2'],
        meta: {
          timestamp: new Date().toISOString(),
          requestId: '123',
          page: 1,
          perPage: 10,
          total: 20,
          totalPages: 2,
          hasNext: true,
          hasPrev: false
        }
      };
      expect(response.success).toBe(true);
      expect(response.data?.length).toBe(2);
      expect(response.meta.page).toBe(1);
      expect(response.meta.hasNext).toBe(true);
    });
  });

  describe('Utility Types', () => {
    it('should create valid Dict', () => {
      const dict: Dict<number> = { test1: 1, test2: 2 };
      expect(dict['test1']).toBe(1);
      expect(dict['test2']).toBe(2);
    });

    it('should create valid List', () => {
      const list: List<string> = ['a', 'b'];
      expect(Array.isArray(list)).toBe(true);
      expect(list.length).toBe(2);
    });

    it('should create valid Tuple', () => {
      const tuple: Tuple<string, number> = ['a', 1];
      expect(tuple[0]).toBe('a');
      expect(tuple[1]).toBe(1);
    });
  });

  describe('File Types', () => {
    it('should create valid FileInfo', () => {
      const file: FileInfo = {
        name: 'test.jpg',
        size: 1024,
        type: 'image/jpeg',
        lastModified: Date.now(),
        url: 'http://example.com/test.jpg',
        preview: 'data:image/jpeg;base64,...',
        progress: 50,
        uploaded: false,
        extension: 'jpg'
      };
      expect(file.name).toBe('test.jpg');
      expect(file.size).toBe(1024);
      expect(file.type).toBe('image/jpeg');
    });
  });

  describe('Form Types', () => {
    it('should create valid FormField', () => {
      const field: FormField<string> = {
        name: 'email',
        label: 'Email',
        value: 'test@example.com',
        type: 'email',
        required: true,
        placeholder: 'Enter email',
        validators: []
      };
      expect(field.name).toBe('email');
      expect(field.value).toBe('test@example.com');
      expect(field.required).toBe(true);
    });
  });

  describe('Error Types', () => {
    it('should create valid ErrorState', () => {
      const error: ErrorState = {
        hasError: true,
        error: new Error('Test error'),
        errorCode: 'E001',
        errorMessage: 'Test error message',
        timestamp: new Date()
      };
      expect(error.hasError).toBe(true);
      expect(error.errorCode).toBe('E001');
    });
  });

  describe('Theme Types', () => {
    it('should create valid ThemeConfig', () => {
      const theme: ThemeConfig = {
        mode: 'light',
        colors: {
          primary: '#007bff',
          secondary: '#6c757d',
          success: '#28a745',
          danger: '#dc3545',
          warning: '#ffc107',
          info: '#17a2b8',
          light: '#f8f9fa',
          dark: '#343a40'
        },
        typography: {
          fontFamily: 'Arial',
          fontSize: '16px',
          lineHeight: '1.5'
        },
        spacing: {
          small: '8px',
          medium: '16px',
          large: '24px'
        },
        breakpoints: {
          sm: '576px',
          md: '768px',
          lg: '992px',
          xl: '1200px'
        }
      };
      expect(theme.mode).toBe('light');
      expect(theme.colors.primary).toBe('#007bff');
      expect(typeof theme.typography.fontSize).toBe('string');
    });
  });
});