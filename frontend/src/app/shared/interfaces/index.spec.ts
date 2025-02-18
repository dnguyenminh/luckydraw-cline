import {
  LoadableComponent,
  DisableableComponent,
  SizeableComponent,
  ThemeableComponent,
  HasIconComponent,
  HasTooltipComponent,
  Action,
  KeyedAction,
  FormField,
  ValidationResult,
  DataSource,
  Selection,
  SortConfig,
  FilterConfig,
  Notification,
  NotificationConfig,
  DialogConfig,
  DialogRef,
  UserInfo,
  AuthState,
  ErrorDetails,
  ErrorState
} from './index';

describe('Shared Interfaces', () => {
  describe('Component Interfaces', () => {
    it('should implement LoadableComponent', () => {
      const component: LoadableComponent = { loading: true };
      expect(component.loading).toBe(true);
    });

    it('should implement DisableableComponent', () => {
      const component: DisableableComponent = { disabled: true };
      expect(component.disabled).toBe(true);
    });

    it('should implement SizeableComponent', () => {
      const component: SizeableComponent = { size: 'medium' };
      expect(component.size).toBe('medium');
    });

    it('should implement ThemeableComponent', () => {
      const component: ThemeableComponent = { theme: 'primary' };
      expect(component.theme).toBe('primary');
    });

    it('should implement HasIconComponent', () => {
      const component: HasIconComponent = {
        icon: 'test-icon',
        iconPosition: 'left'
      };
      expect(component.icon).toBe('test-icon');
      expect(component.iconPosition).toBe('left');
    });

    it('should implement HasTooltipComponent', () => {
      const component: HasTooltipComponent = {
        tooltip: 'Test tooltip',
        tooltipPlacement: 'top'
      };
      expect(component.tooltip).toBe('Test tooltip');
      expect(component.tooltipPlacement).toBe('top');
    });
  });

  describe('Action Interfaces', () => {
    it('should implement Action', () => {
      const action: Action = {
        label: 'Test Action',
        handler: () => {},
        disabled: false,
        icon: 'test-icon'
      };
      expect(action.label).toBe('Test Action');
      expect(typeof action.handler).toBe('function');
    });

    it('should implement KeyedAction', () => {
      const action: KeyedAction = {
        key: 'test',
        label: 'Test Action',
        handler: () => {}
      };
      expect(action.key).toBe('test');
    });
  });

  describe('Form Interfaces', () => {
    it('should implement FormField', () => {
      const field: FormField = {
        key: 'email',
        label: 'Email',
        type: 'email',
        required: true
      };
      expect(field.key).toBe('email');
      expect(field.type).toBe('email');
    });

    it('should implement ValidationResult', () => {
      const result: ValidationResult = {
        valid: false,
        errors: { }
      };
      result.errors!['email'] = 'Invalid email';
      
      expect(result.valid).toBe(false);
      expect(result.errors!['email']).toBe('Invalid email');
    });
  });

  describe('Data Interfaces', () => {
    it('should implement DataSource', () => {
      const dataSource: DataSource<string> = {
        data: ['item1', 'item2'],
        loading: false,
        metadata: { total: 2 }
      };
      expect(dataSource.data.length).toBe(2);
      expect(dataSource.metadata?.total).toBe(2);
    });

    it('should implement Selection', () => {
      const selection: Selection<string> = {
        selected: ['item1'],
        selectionMode: 'single',
        onSelect: () => {}
      };
      expect(selection.selected).toContain('item1');
      expect(selection.selectionMode).toBe('single');
    });

    it('should implement SortConfig', () => {
      const sort: SortConfig = {
        active: 'name',
        direction: 'asc'
      };
      expect(sort.active).toBe('name');
      expect(sort.direction).toBe('asc');
    });

    it('should implement FilterConfig', () => {
      const filter: FilterConfig = {
        field: 'name',
        operator: 'contains',
        value: 'test'
      };
      expect(filter.field).toBe('name');
      expect(filter.operator).toBe('contains');
    });
  });

  describe('Notification Interfaces', () => {
    it('should implement Notification', () => {
      const notification: Notification = {
        type: 'success',
        message: 'Test message'
      };
      expect(notification.type).toBe('success');
      expect(notification.message).toBe('Test message');
    });

    it('should implement NotificationConfig', () => {
      const config: NotificationConfig = {
        position: 'top-right',
        timeout: 3000
      };
      expect(config.position).toBe('top-right');
      expect(config.timeout).toBe(3000);
    });
  });

  describe('Dialog Interfaces', () => {
    it('should implement DialogConfig', () => {
      const config: DialogConfig = {
        title: 'Test Dialog',
        size: 'md',
        centered: true
      };
      expect(config.title).toBe('Test Dialog');
      expect(config.size).toBe('md');
    });

    it('should implement DialogRef', () => {
      const dialogRef: DialogRef = {
        close: () => {},
        dismiss: () => {}
      };
      expect(typeof dialogRef.close).toBe('function');
      expect(typeof dialogRef.dismiss).toBe('function');
    });
  });

  describe('Auth Interfaces', () => {
    it('should implement UserInfo', () => {
      const user: UserInfo = {
        id: '1',
        username: 'test',
        email: 'test@example.com',
        roles: ['USER'],
        permissions: ['READ']
      };
      expect(user.username).toBe('test');
      expect(user.roles).toContain('USER');
    });

    it('should implement AuthState', () => {
      const auth: AuthState = {
        authenticated: true,
        user: {
          id: '1',
          username: 'test',
          email: 'test@example.com',
          roles: [],
          permissions: []
        }
      };
      expect(auth.authenticated).toBe(true);
      expect(auth.user?.username).toBe('test');
    });
  });

  describe('Error Interfaces', () => {
    it('should implement ErrorDetails', () => {
      const error: ErrorDetails = {
        code: 'ERR_001',
        message: 'Test error'
      };
      expect(error.code).toBe('ERR_001');
      expect(error.message).toBe('Test error');
    });

    it('should implement ErrorState', () => {
      const state: ErrorState = {
        hasError: true,
        error: {
          code: 'ERR_001',
          message: 'Test error'
        }
      };
      expect(state.hasError).toBe(true);
      expect(state.error?.code).toBe('ERR_001');
    });
  });
});