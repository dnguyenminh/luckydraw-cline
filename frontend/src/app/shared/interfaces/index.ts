// Common interfaces for components
export interface LoadableComponent {
  loading?: boolean;
}

export interface DisableableComponent {
  disabled?: boolean;
}

export interface SizeableComponent {
  size?: 'small' | 'medium' | 'large';
}

export interface ThemeableComponent {
  theme?: 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info';
}

export interface HasIconComponent {
  icon?: string;
  iconPosition?: 'left' | 'right';
}

export interface HasTooltipComponent {
  tooltip?: string;
  tooltipPlacement?: 'top' | 'right' | 'bottom' | 'left';
}

// Common interfaces for actions/events
export interface Action {
  id?: string;
  label: string;
  icon?: string;
  handler: () => void;
  disabled?: boolean;
  hidden?: boolean;
  tooltip?: string;
  class?: string;
  busy?: boolean;
}

export interface KeyedAction extends Action {
  key: string;
}

// Common interfaces for forms
export interface FormField<T = any> {
  key: string;
  label: string;
  type: 'text' | 'number' | 'email' | 'password' | 'select' | 'textarea' | 'checkbox' | 'radio';
  value?: T;
  placeholder?: string;
  validators?: any[];
  disabled?: boolean;
  required?: boolean;
  hint?: string;
  errorMessage?: string;
  options?: Array<{ label: string; value: any }>;
}

export interface ValidationResult {
  valid: boolean;
  errors?: { [key: string]: string };
}

// Common interfaces for data
export interface DataSource<T> {
  data: T[];
  loading?: boolean;
  error?: Error;
  metadata?: {
    total?: number;
    page?: number;
    limit?: number;
  };
}

export interface Selection<T> {
  selected: T[];
  selectionMode: 'single' | 'multiple';
  maxSelections?: number;
  onSelect: (items: T[]) => void;
}

export interface SortConfig {
  active: string;
  direction: 'asc' | 'desc' | '';
}

export interface FilterConfig {
  field: string;
  operator: 'eq' | 'neq' | 'gt' | 'gte' | 'lt' | 'lte' | 'contains' | 'startsWith' | 'endsWith';
  value: any;
}

// Common interfaces for notifications
export interface Notification {
  id?: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  title?: string;
  timeout?: number;
  dismissible?: boolean;
  actions?: Action[];
}

export interface NotificationConfig {
  position?: 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right' | 'top-center' | 'bottom-center';
  timeout?: number;
  maxItems?: number;
  duplicates?: boolean;
  animation?: 'fade' | 'slide' | 'none';
}

// Common interfaces for modal/dialog
export interface DialogConfig {
  title?: string;
  message?: string;
  confirmText?: string;
  cancelText?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl' | 'fullscreen';
  scrollable?: boolean;
  centered?: boolean;
  backdrop?: boolean | 'static';
  keyboard?: boolean;
  animation?: boolean;
}

export interface DialogRef<T = any> {
  close: (result?: T) => void;
  dismiss: (reason?: any) => void;
}

// Common interfaces for auth/user
export interface UserInfo {
  id: string;
  username: string;
  email: string;
  roles: string[];
  permissions: string[];
  metadata?: Record<string, any>;
}

export interface AuthState {
  authenticated: boolean;
  user?: UserInfo;
  token?: string;
  error?: string;
}

// Common interfaces for error handling
export interface ErrorDetails {
  code: string;
  message: string;
  field?: string;
  details?: Record<string, any>;
}

export interface ErrorState {
  hasError: boolean;
  error?: ErrorDetails;
  timestamp?: number;
}