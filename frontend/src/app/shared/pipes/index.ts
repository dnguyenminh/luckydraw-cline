// Export pipe interfaces and types
export {
  PipeConfig,
  RelativeTimeConfig,
  FileSizeConfig,
  TruncateConfig,
  BinaryUnit,
  DecimalUnit,
  NumberFormatConfig,
  DateFormatConfig
} from './interfaces';

// Import and re-export pipe configuration defaults
import { PIPE_DEFAULTS, type PipeDefaults } from './pipe-defaults';
export { PIPE_DEFAULTS };

// Import pipe classes
import { RelativeTimePipe } from './relative-time.pipe';
import { FileSizePipe } from './file-size.pipe';
import { TruncatePipe } from './truncate.pipe';

// Export pipe classes
export { RelativeTimePipe, FileSizePipe, TruncatePipe };

/**
 * Format a relative time string (e.g., "2 hours ago")
 */
export function formatRelativeTime(
  date: Date | number,
  config: Partial<PipeDefaults['relativeTime']> = {}
): string {
  const pipe = new RelativeTimePipe();
  return pipe.transform(date, { ...PIPE_DEFAULTS.relativeTime, ...config });
}

/**
 * Format a file size string (e.g., "1.5 MB")
 */
export function formatFileSize(
  bytes: number,
  config: Partial<PipeDefaults['fileSize']> = {}
): string {
  const pipe = new FileSizePipe();
  return pipe.transform(bytes, { ...PIPE_DEFAULTS.fileSize, ...config });
}

/**
 * Format a truncated string with optional configuration
 */
export function formatTruncatedText(
  text: string,
  config: Partial<PipeDefaults['truncate']> = {}
): string {
  const pipe = new TruncatePipe();
  return pipe.transform(text, { ...PIPE_DEFAULTS.truncate, ...config });
}

// Type guards for configuration objects
export function isRelativeTimeConfig(
  config: any
): config is PipeDefaults['relativeTime'] {
  if (!config || typeof config !== 'object') return false;
  const validKeys = ['addSuffix', 'maxUnits', 'roundUp', 'largestUnit', 'smallestUnit', 'locale'];
  return validKeys.some(key => key in config);
}

export function isFileSizeConfig(
  config: any
): config is PipeDefaults['fileSize'] {
  if (!config || typeof config !== 'object') return false;
  const validKeys = ['binary', 'decimals', 'stripZeros', 'largestUnit', 'spaceBetween', 'locale'];
  return validKeys.some(key => key in config);
}

export function isTruncateConfig(
  config: any
): config is PipeDefaults['truncate'] {
  if (!config || typeof config !== 'object') return false;
  const validKeys = ['length', 'suffix', 'wordBoundary', 'fromStart', 'stripTags'];
  return validKeys.some(key => key in config);
}

// Export pipe types
export type PipeTypes = {
  relativeTime: PipeDefaults['relativeTime'];
  fileSize: PipeDefaults['fileSize'];
  truncate: PipeDefaults['truncate'];
  number: PipeDefaults['number'];
  date: PipeDefaults['date'];
};