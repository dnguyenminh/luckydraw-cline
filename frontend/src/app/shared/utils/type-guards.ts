import {
  Dict,
  ApiResponse,
  PaginatedResponse,
  FileInfo,
  FormField,
  ErrorState,
  ThemeConfig
} from '../types';

/**
 * Type guard for checking if a value is null or undefined
 */
export function isNullOrUndefined(value: unknown): value is null | undefined {
  return value === null || value === undefined;
}

/**
 * Type guard for checking if a value is a string
 */
export function isString(value: unknown): value is string {
  return typeof value === 'string';
}

/**
 * Type guard for checking if a value is a number
 */
export function isNumber(value: unknown): value is number {
  return typeof value === 'number' && !isNaN(value);
}

/**
 * Type guard for checking if a value is a boolean
 */
export function isBoolean(value: unknown): value is boolean {
  return typeof value === 'boolean';
}

/**
 * Type guard for checking if a value is a Date
 */
export function isDate(value: unknown): value is Date {
  return value instanceof Date && !isNaN(value.getTime());
}

/**
 * Type guard for checking if a value is an object
 */
export function isObject(value: unknown): value is object {
  return typeof value === 'object' && value !== null;
}

/**
 * Type guard for checking if a value is an array
 */
export function isArray(value: unknown): value is unknown[] {
  return Array.isArray(value);
}

/**
 * Type guard for checking if a value is a Dict
 */
export function isDict<T = any>(value: unknown): value is Dict<T> {
  return isObject(value) && !isArray(value);
}

/**
 * Type guard for checking if a value is a Promise
 */
export function isPromise<T = any>(value: unknown): value is Promise<T> {
  return value instanceof Promise;
}

/**
 * Type guard for checking if a value is a function
 */
export function isFunction(value: unknown): value is Function {
  return typeof value === 'function';
}

/**
 * Type guard for checking if a value is an API response
 */
export function isApiResponse<T = any>(value: unknown): value is ApiResponse<T> {
  return (
    isObject(value) &&
    'success' in value &&
    'meta' in value &&
    isObject(value.meta) &&
    'timestamp' in value.meta &&
    'requestId' in value.meta
  );
}

/**
 * Type guard for checking if a value is a paginated response
 */
export function isPaginatedResponse<T = any>(value: unknown): value is PaginatedResponse<T> {
  return (
    isApiResponse(value) &&
    'meta' in value &&
    isObject(value.meta) &&
    'page' in value.meta &&
    'perPage' in value.meta &&
    'total' in value.meta &&
    'totalPages' in value.meta &&
    'hasNext' in value.meta &&
    'hasPrev' in value.meta
  );
}

/**
 * Type guard for checking if a value is a file info object
 */
export function isFileInfo(value: unknown): value is FileInfo {
  return (
    isObject(value) &&
    'name' in value &&
    'size' in value &&
    'type' in value &&
    'lastModified' in value
  );
}

/**
 * Type guard for checking if a value is a form field
 */
export function isFormField<T = any>(value: unknown): value is FormField<T> {
  return (
    isObject(value) &&
    'name' in value &&
    'label' in value &&
    'value' in value &&
    'type' in value
  );
}

/**
 * Type guard for checking if a value is an error state
 */
export function isErrorState(value: unknown): value is ErrorState {
  return (
    isObject(value) &&
    'hasError' in value &&
    typeof value.hasError === 'boolean'
  );
}

/**
 * Type guard for checking if a value is a theme config
 */
export function isThemeConfig(value: unknown): value is ThemeConfig {
  return (
    isObject(value) &&
    'mode' in value &&
    'colors' in value &&
    'typography' in value &&
    'spacing' in value &&
    'breakpoints' in value
  );
}

/**
 * Type guard for checking if a value is a FormData instance
 */
export function isFormData(value: unknown): value is FormData {
  return value instanceof FormData;
}

/**
 * Type guard for checking if a value is a File instance
 */
export function isFile(value: unknown): value is File {
  return value instanceof File;
}

/**
 * Type guard for checking if a value is a Blob instance
 */
export function isBlob(value: unknown): value is Blob {
  return value instanceof Blob;
}

/**
 * Type guard for checking if an array contains only strings
 */
export function isStringArray(value: unknown[]): value is string[] {
  return value.every(isString);
}

/**
 * Type guard for checking if an array contains only numbers
 */
export function isNumberArray(value: unknown[]): value is number[] {
  return value.every(isNumber);
}

/**
 * Type guard for checking if a value is not null or undefined
 */
export function isNonNull<T>(value: T | null | undefined): value is T {
  return !isNullOrUndefined(value);
}