/**
 * Configuration for pipe formatting
 */
export interface PipeConfig {
  /**
   * Locale for formatting
   */
  locale?: string;
}

/**
 * Configuration for relative time formatting
 */
export interface RelativeTimeConfig extends PipeConfig {
  /**
   * Whether to include 'ago' or 'from now' suffix
   */
  addSuffix?: boolean;

  /**
   * Maximum number of units to display
   */
  maxUnits?: number;

  /**
   * Whether to round up values
   */
  roundUp?: boolean;

  /**
   * Largest unit to use (year, month, day, hour, minute, second)
   */
  largestUnit?: 'year' | 'month' | 'day' | 'hour' | 'minute' | 'second';

  /**
   * Smallest unit to use (year, month, day, hour, minute, second)
   */
  smallestUnit?: 'year' | 'month' | 'day' | 'hour' | 'minute' | 'second';
}

/**
 * Binary file size units
 */
export type BinaryUnit = 'B' | 'KiB' | 'MiB' | 'GiB' | 'TiB' | 'PiB' | 'EiB' | 'ZiB' | 'YiB';

/**
 * Decimal file size units
 */
export type DecimalUnit = 'B' | 'KB' | 'MB' | 'GB' | 'TB' | 'PB' | 'EB' | 'ZB' | 'YB';

/**
 * Configuration for file size formatting
 */
export interface FileSizeConfig extends PipeConfig {
  /**
   * Number of decimal places to display
   */
  decimals?: number;

  /**
   * Whether to use binary (1024) or decimal (1000) units
   */
  binary?: boolean;

  /**
   * Largest unit to display
   */
  largestUnit?: BinaryUnit | DecimalUnit;

  /**
   * Whether to strip trailing zeros
   */
  stripZeros?: boolean;

  /**
   * Whether to include space between number and unit
   */
  spaceBetween?: boolean;
}

/**
 * Configuration for text truncation
 */
export interface TruncateConfig {
  /**
   * Length to truncate text to
   */
  length: number;

  /**
   * String to use as suffix when text is truncated
   */
  suffix?: string;

  /**
   * Whether to truncate at word boundaries
   */
  wordBoundary?: boolean;

  /**
   * Whether to truncate from the start of the string
   */
  fromStart?: boolean;

  /**
   * Whether to strip HTML tags
   */
  stripTags?: boolean;
}

/**
 * Configuration for number formatting
 */
export interface NumberFormatConfig extends PipeConfig {
  /**
   * Minimum number of decimal places
   */
  minimumFractionDigits?: number;

  /**
   * Maximum number of decimal places
   */
  maximumFractionDigits?: number;

  /**
   * Whether to use grouping separator
   */
  useGrouping?: boolean;

  /**
   * Style to use for formatting (decimal, currency, percent)
   */
  style?: 'decimal' | 'currency' | 'percent';

  /**
   * Currency code to use when style is 'currency'
   */
  currency?: string;

  /**
   * How to display the currency
   */
  currencyDisplay?: 'symbol' | 'code' | 'name';
}

/**
 * Configuration for date formatting
 */
export interface DateFormatConfig extends PipeConfig {
  /**
   * Date format pattern
   */
  format?: string;

  /**
   * Time zone to use for formatting
   */
  timeZone?: string;

  /**
   * Whether to include time in the output
   */
  showTime?: boolean;

  /**
   * Whether to use 24-hour time format
   */
  use24Hour?: boolean;

  /**
   * Whether to show seconds
   */
  showSeconds?: boolean;
}