/**
 * Common utility functions
 */

/**
 * Format a number with specified decimals and locale
 */
export function formatNumber(
  value: number,
  decimals: number = 0,
  locale: string = 'en-US'
): string {
  return new Intl.NumberFormat(locale, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  }).format(value);
}

/**
 * Capitalize first letter of a string
 */
export function capitalize(str: string): string {
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}

/**
 * Deep clone an object
 */
export function deepClone<T>(obj: T): T {
  if (obj === null || typeof obj !== 'object') return obj;
  
  if (Array.isArray(obj)) {
    return obj.map(deepClone) as unknown as T;
  }
  
  return Object.fromEntries(
    Object.entries(obj as object).map(([key, value]) => [key, deepClone(value)])
  ) as T;
}

/**
 * Parse query string to object
 */
export function parseQueryString(queryString: string): Record<string, string> {
  const params = new URLSearchParams(queryString.startsWith('?') ? queryString.substring(1) : queryString);
  const result: Record<string, string> = {};
  
  params.forEach((value, key) => {
    result[key] = value;
  });
  
  return result;
}

/**
 * Build query string from object
 */
export function buildQueryString(params: Record<string, string | number | boolean>): string {
  const searchParams = new URLSearchParams();
  
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      searchParams.append(key, String(value));
    }
  });
  
  const queryString = searchParams.toString();
  return queryString ? `?${queryString}` : '';
}

/**
 * Clamp a number between min and max values
 */
export function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}

/**
 * Round a number to specified precision
 */
export function round(value: number, precision: number = 0): number {
  const multiplier = Math.pow(10, precision);
  return Math.round(value * multiplier) / multiplier;
}

/**
 * Generate a unique ID with optional prefix
 */
export function generateId(prefix: string = ''): string {
  return `${prefix}${Math.random().toString(36).substring(2)}${Date.now().toString(36)}`;
}

/**
 * Check if a value is empty (null, undefined, empty string, empty array, empty object)
 */
export function isEmpty(value: any): boolean {
  if (value === null || value === undefined) return true;
  if (typeof value === 'string') return value.trim().length === 0;
  if (Array.isArray(value)) return value.length === 0;
  if (typeof value === 'object') return Object.keys(value).length === 0;
  return false;
}

/**
 * Safe access to nested object properties
 */
export function get<T>(obj: any, path: string, defaultValue?: T): T | undefined {
  const keys = path.split('.');
  let result = obj;
  
  for (const key of keys) {
    if (result === null || result === undefined) return defaultValue;
    result = result[key];
  }
  
  return (result === undefined) ? defaultValue : result;
}

/**
 * Convert bytes to human readable file size
 */
export function formatFileSize(bytes: number, binary: boolean = true): string {
  if (bytes === 0) return '0 B';
  
  const base = binary ? 1024 : 1000;
  const units = binary
    ? ['B', 'KiB', 'MiB', 'GiB', 'TiB', 'PiB']
    : ['B', 'KB', 'MB', 'GB', 'TB', 'PB'];
  
  const exponent = Math.floor(Math.log(bytes) / Math.log(base));
  const value = bytes / Math.pow(base, exponent);
  
  return `${round(value, 2)} ${units[exponent]}`;
}

/**
 * Check if value is a promise
 */
export function isPromise<T = any>(value: any): value is Promise<T> {
  return Boolean(value && typeof value.then === 'function');
}

/**
 * Create a promise that rejects after a timeout
 */
export function timeout<T>(promise: Promise<T>, ms: number): Promise<T> {
  const timeoutPromise = new Promise<T>((_, reject) => {
    setTimeout(() => reject(new Error(`Operation timed out after ${ms}ms`)), ms);
  });
  
  return Promise.race([promise, timeoutPromise]);
}