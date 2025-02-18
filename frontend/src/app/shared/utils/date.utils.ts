/**
 * Date manipulation utilities
 */

/**
 * Add days to a date
 */
export function addDays(date: Date, days: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

/**
 * Get start of day
 */
export function startOfDay(date: Date): Date {
  const result = new Date(date);
  result.setHours(0, 0, 0, 0);
  return result;
}

/**
 * Get end of day
 */
export function endOfDay(date: Date): Date {
  const result = new Date(date);
  result.setHours(23, 59, 59, 999);
  return result;
}

/**
 * Get start of week (Sunday)
 */
export function startOfWeek(date: Date): Date {
  const result = new Date(date);
  const day = result.getDay();
  const diff = result.getDate() - day;
  result.setDate(diff);
  result.setHours(0, 0, 0, 0);
  return result;
}

/**
 * Get end of week (Saturday)
 */
export function endOfWeek(date: Date): Date {
  const result = new Date(date);
  const day = result.getDay();
  const diff = result.getDate() + (6 - day);
  result.setDate(diff);
  result.setHours(23, 59, 59, 999);
  return result;
}

/**
 * Get start of month
 */
export function startOfMonth(date: Date): Date {
  const result = new Date(date);
  result.setDate(1);
  result.setHours(0, 0, 0, 0);
  return result;
}

/**
 * Get end of month
 */
export function endOfMonth(date: Date): Date {
  const result = new Date(date);
  result.setMonth(result.getMonth() + 1);
  result.setDate(0);
  result.setHours(23, 59, 59, 999);
  return result;
}

/**
 * Check if date is between two dates
 */
export function isBetween(
  date: Date,
  start: Date,
  end: Date,
  inclusive: boolean = true
): boolean {
  const timestamp = date.getTime();
  if (inclusive) {
    return timestamp >= start.getTime() && timestamp <= end.getTime();
  }
  return timestamp > start.getTime() && timestamp < end.getTime();
}

/**
 * Format date to string with specified format
 */
export function formatDate(
  date: Date,
  format: string = 'yyyy-MM-dd',
  locale: string = 'en-US'
): string {
  if (!date) return '';
  
  const options: Intl.DateTimeFormatOptions = {};
  
  if (format.includes('yyyy')) options.year = 'numeric';
  if (format.includes('MM')) options.month = '2-digit';
  if (format.includes('dd')) options.day = '2-digit';
  if (format.includes('HH')) options.hour = '2-digit';
  if (format.includes('mm')) options.minute = '2-digit';
  if (format.includes('ss')) options.second = '2-digit';
  
  return new Intl.DateTimeFormat(locale, options).format(date);
}

/**
 * Parse date from string
 */
export function parseDate(value: string): Date | null {
  const date = new Date(value);
  return isNaN(date.getTime()) ? null : date;
}

/**
 * Get relative time string (e.g., "2 hours ago")
 */
export function getRelativeTime(
  date: Date,
  locale: string = 'en-US'
): string {
  const rtf = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' });
  const now = new Date();
  const diff = date.getTime() - now.getTime();
  
  const seconds = Math.round(diff / 1000);
  const minutes = Math.round(seconds / 60);
  const hours = Math.round(minutes / 60);
  const days = Math.round(hours / 24);
  const months = Math.round(days / 30);
  const years = Math.round(months / 12);
  
  if (Math.abs(years) >= 1) return rtf.format(years, 'year');
  if (Math.abs(months) >= 1) return rtf.format(months, 'month');
  if (Math.abs(days) >= 1) return rtf.format(days, 'day');
  if (Math.abs(hours) >= 1) return rtf.format(hours, 'hour');
  if (Math.abs(minutes) >= 1) return rtf.format(minutes, 'minute');
  return rtf.format(seconds, 'second');
}

/**
 * Get days in month
 */
export function getDaysInMonth(date: Date): number {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
}

/**
 * Check if year is leap year
 */
export function isLeapYear(year: number): boolean {
  return year % 400 === 0 || (year % 100 !== 0 && year % 4 === 0);
}

/**
 * Get week number in year
 */
export function getWeekNumber(date: Date): number {
  const target = new Date(date.valueOf());
  const dayNr = (date.getDay() + 6) % 7;
  target.setDate(target.getDate() - dayNr + 3);
  const firstThursday = target.valueOf();
  target.setMonth(0, 1);
  if (target.getDay() !== 4) {
    target.setMonth(0, 1 + ((4 - target.getDay()) + 7) % 7);
  }
  return 1 + Math.ceil((firstThursday - target.valueOf()) / 604800000);
}

/**
 * Get quarter number (1-4)
 */
export function getQuarter(date: Date): number {
  return Math.floor(date.getMonth() / 3) + 1;
}