/**
 * Common type definitions used across the application
 */

// Basic utility types
export type Dict<T = any> = { [key: string]: T };
export type List<T = any> = Array<T>;
export type Tuple<T = any, U = any> = [T, U];
export type Maybe<T> = T | null | undefined;
export type Optional<T> = T | undefined;

// Complex utility types
export type DeepPartial<T> = {
  [P in keyof T]?: T[P] extends object ? DeepPartial<T[P]> : T[P];
};

export type RecursivePartial<T> = {
  [P in keyof T]?: T[P] extends (infer U)[]
    ? RecursivePartial<U>[]
    : T[P] extends object
    ? RecursivePartial<T[P]>
    : T[P];
};

// Function types
export type AsyncFunction<T = any> = (...args: any[]) => Promise<T>;
export type SyncFunction<T = any> = (...args: any[]) => T;
export type ErrorHandler = (error: Error) => void;
export type SuccessHandler<T = any> = (result: T) => void;

// Component types
export type ModalSize = 'sm' | 'md' | 'lg' | 'xl' | 'full';
export type ComponentSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl';
export type ComponentTheme = 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'light' | 'dark' | 'system';
export type AlertType = 'success' | 'error' | 'warning' | 'info';
export type SortDirection = 'asc' | 'desc';

// HTTP types
export interface ApiMeta {
  timestamp: string;
  requestId: string;
  [key: string]: any;
}

export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  error?: {
    code: string;
    message: string;
    details?: any;
  };
  meta: ApiMeta;
}

export interface PaginationMeta extends ApiMeta {
  page: number;
  perPage: number;
  total: number;
  totalPages: number;
  hasNext: boolean;
  hasPrev: boolean;
  items?: any[];
}

export interface PaginatedResponse<T = any> extends Omit<ApiResponse<T[]>, 'meta'> {
  meta: PaginationMeta;
}

// Form types
export interface FormField<T = any> {
  name: string;
  label: string;
  value: T;
  type: string;
  required?: boolean;
  disabled?: boolean;
  hidden?: boolean;
  placeholder?: string;
  helpText?: string;
  errorMessage?: string;
  validators?: any[];
  options?: Array<{
    label: string;
    value: any;
    disabled?: boolean;
  }>;
  config?: {
    [key: string]: any;
  };
}

// Error types
export interface ErrorState {
  hasError: boolean;
  error?: Error | null;
  errorCode?: string;
  errorMessage?: string;
  errorDetails?: any;
  timestamp?: Date;
}

// Selection types
export interface Selection<T = any> {
  selected: T[];
  deselected: T[];
  all: boolean;
}

// Sort types
export interface SortConfig {
  field: string;
  direction: SortDirection;
}

// Filter types
export interface FilterConfig {
  field: string;
  operator: 'eq' | 'neq' | 'gt' | 'gte' | 'lt' | 'lte' | 'like' | 'in' | 'nin';
  value: any;
}

// File types
export interface FileInfo {
  name: string;
  size: number;
  type: string;
  lastModified: number;
  path?: string;
  url?: string;
  preview?: string;
  progress?: number;
  error?: string;
  uploaded?: boolean;
  extension?: string;
  metadata?: {
    [key: string]: any;
  };
}

// Chart types
export type ChartType = 'line' | 'bar' | 'pie' | 'doughnut' | 'radar' | 'scatter';

export interface ChartData {
  labels: string[];
  datasets: Array<{
    label: string;
    data: number[];
    backgroundColor?: string | string[];
    borderColor?: string | string[];
    [key: string]: any;
  }>;
  options?: {
    [key: string]: any;
  };
}

// Theme types
export interface ThemeConfig {
  mode: 'light' | 'dark' | 'system';
  colors: {
    primary: string;
    secondary: string;
    success: string;
    danger: string;
    warning: string;
    info: string;
    light: string;
    dark: string;
    [key: string]: string;
  };
  typography: {
    fontFamily: string;
    fontSize: string;
    lineHeight: string;
    [key: string]: string;
  };
  spacing: {
    [key: string]: string;
  };
  breakpoints: {
    [key: string]: string;
  };
}