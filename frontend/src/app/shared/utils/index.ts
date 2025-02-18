// Re-export type guards
export {
  isString,
  isNumber,
  isBoolean,
  isDate,
  isObject,
  isArray,
  isDict,
  isFunction,
  isApiResponse,
  isPaginatedResponse,
  isFileInfo,
  isFormField,
  isErrorState,
  isThemeConfig,
  isFormData,
  isFile,
  isBlob,
  isStringArray,
  isNumberArray,
  isNonNull,
  isNullOrUndefined
} from './type-guards';

// Re-export string utilities
export {
  camelToKebab,
  kebabToCamel,
  truncate,
  stripHtml,
  slugify,
  escapeRegExp,
  randomString,
  getInitials,
  pluralize,
  countOccurrences
} from './string.utils';

// Re-export array utilities
export {
  chunk,
  unique,
  groupBy,
  pick,
  omit,
  sortBy,
  difference,
  intersection,
  shuffle,
  compact,
  sample,
  sampleSize,
  moveItem,
  range,
  countBy
} from './array.utils';

// Re-export date utilities
export {
  addDays,
  startOfDay,
  endOfDay,
  startOfWeek,
  endOfWeek,
  startOfMonth,
  endOfMonth,
  isBetween,
  formatDate,
  parseDate,
  getRelativeTime,
  getDaysInMonth,
  isLeapYear,
  getWeekNumber,
  getQuarter
} from './date.utils';

// Re-export common utilities
export {
  formatNumber,
  capitalize,
  deepClone,
  parseQueryString,
  buildQueryString,
  clamp,
  round,
  generateId,
  isEmpty,
  get,
  formatFileSize,
  isPromise,
  timeout
} from './common.utils';

/**
 * Create a debounced function
 */
export const debounce = <T extends (...args: any[]) => any>(
  fn: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeoutId: ReturnType<typeof setTimeout>;
  return (...args: Parameters<T>) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => fn(...args), wait);
  };
};

/**
 * Create a throttled function
 */
export const throttle = <T extends (...args: any[]) => any>(
  fn: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let waiting = false;
  return (...args: Parameters<T>) => {
    if (!waiting) {
      fn(...args);
      waiting = true;
      setTimeout(() => {
        waiting = false;
      }, wait);
    }
  };
};

/**
 * Delay execution for specified milliseconds
 */
export const delay = (ms: number): Promise<void> =>
  new Promise(resolve => setTimeout(resolve, ms));

/**
 * Retry a function with exponential backoff
 */
export const retry = async <T>(
  fn: () => Promise<T>,
  maxAttempts: number = 3,
  baseDelay: number = 1000
): Promise<T> => {
  let lastError: Error;
  
  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    try {
      return await fn();
    } catch (error) {
      lastError = error as Error;
      if (attempt === maxAttempts) break;
      
      const delay = baseDelay * Math.pow(2, attempt - 1);
      await new Promise(resolve => setTimeout(resolve, delay));
    }
  }
  
  throw lastError!;
};

/**
 * Memoize a function
 */
export const memoize = <T extends (...args: any[]) => any>(
  fn: T,
  { maxSize = 100 }: { maxSize?: number } = {}
): T => {
  const cache = new Map<string, { value: ReturnType<T>; timestamp: number }>();
  
  return ((...args: Parameters<T>): ReturnType<T> => {
    const key = JSON.stringify(args);
    
    if (cache.has(key)) {
      return cache.get(key)!.value;
    }
    
    const result = fn(...args);
    
    if (cache.size >= maxSize) {
      // Remove oldest entry
      const oldest = [...cache.entries()].sort(
        (a, b) => a[1].timestamp - b[1].timestamp
      )[0];
      cache.delete(oldest[0]);
    }
    
    cache.set(key, { value: result, timestamp: Date.now() });
    return result;
  }) as T;
};