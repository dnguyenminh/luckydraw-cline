/**
 * String manipulation utilities
 */

/**
 * Convert camelCase to kebab-case
 */
export const camelToKebab = (str: string): string =>
  str.replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase();

/**
 * Convert kebab-case to camelCase
 */
export const kebabToCamel = (str: string): string =>
  str.replace(/-([a-z0-9])/g, g => g[1].toUpperCase());

/**
 * Truncate text to a specific length
 */
export const truncate = (
  text: string,
  length: number = 50,
  suffix: string = '...'
): string => {
  if (text.length <= length) return text;
  return text.substring(0, length).trim() + suffix;
};

/**
 * Format file size to human readable format
 */
export const formatFileSize = (bytes: number, binary: boolean = true): string => {
  if (bytes === 0) return '0 B';
  
  const base = binary ? 1024 : 1000;
  const units = binary
    ? ['B', 'KiB', 'MiB', 'GiB', 'TiB', 'PiB']
    : ['B', 'KB', 'MB', 'GB', 'TB', 'PB'];
  
  const exponent = Math.floor(Math.log(bytes) / Math.log(base));
  const value = bytes / Math.pow(base, exponent);
  
  return `${value.toFixed(2)} ${units[exponent]}`;
};

/**
 * Strip HTML tags from a string
 */
export const stripHtml = (html: string): string => {
  const tmp = document.createElement('div');
  tmp.innerHTML = html;
  return tmp.textContent || tmp.innerText || '';
};

/**
 * Convert string to slug format
 */
export const slugify = (text: string): string =>
  text
    .toString()
    .toLowerCase()
    .trim()
    .replace(/\s+/g, '-')
    .replace(/[^\w-]+/g, '')
    .replace(/--+/g, '-');

/**
 * Escape special characters for use in a regular expression
 */
export const escapeRegExp = (text: string): string =>
  text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

/**
 * Generate a random string of specified length
 */
export const randomString = (length: number = 8): string => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  return Array.from(
    { length },
    () => chars[Math.floor(Math.random() * chars.length)]
  ).join('');
};

/**
 * Extract initials from a name
 */
export const getInitials = (name: string): string =>
  name
    .split(/\s+/)
    .map(word => word[0])
    .join('')
    .toUpperCase()
    .substring(0, 2);

/**
 * Pluralize a word based on count
 */
export const pluralize = (
  word: string,
  count: number,
  plural?: string
): string => {
  if (count === 1) return word;
  return plural || word + 's';
};

/**
 * Count occurrences of a substring in a string
 */
export const countOccurrences = (str: string, searchStr: string): number => {
  let count = 0;
  let position = 0;
  while (true) {
    position = str.indexOf(searchStr, position);
    if (position === -1) break;
    count++;
    position += searchStr.length;
  }
  return count;
};
