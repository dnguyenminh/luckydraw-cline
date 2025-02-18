/**
 * Common configuration interfaces for pipes
 */
export interface RelativeTimeDefaults {
  addSuffix: boolean;
  maxUnits: number;
  roundUp: boolean;
  largestUnit: 'year' | 'month' | 'day' | 'hour' | 'minute' | 'second';
  smallestUnit: 'year' | 'month' | 'day' | 'hour' | 'minute' | 'second';
  locale: string;
}

export interface FileSizeDefaults {
  decimals: number;
  binary: boolean;
  largestUnit: 'B' | 'KiB' | 'MiB' | 'GiB' | 'TiB' | 'PiB' | 'EiB' | 'ZiB' | 'YiB';
  stripZeros: boolean;
  spaceBetween: boolean;
  locale: string;
}

export interface TruncateDefaults {
  length: number;
  suffix: string;
  wordBoundary: boolean;
  fromStart: boolean;
  stripTags: boolean;
}

export interface NumberDefaults {
  minimumFractionDigits: number;
  maximumFractionDigits: number;
  useGrouping: boolean;
  style: 'decimal' | 'currency' | 'percent';
  locale: string;
}

export interface DateDefaults {
  format: string;
  showTime: boolean;
  use24Hour: boolean;
  showSeconds: boolean;
  locale: string;
}

/**
 * Combined type for all pipe defaults
 */
export type PipeDefaults = {
  readonly relativeTime: RelativeTimeDefaults;
  readonly fileSize: FileSizeDefaults;
  readonly truncate: TruncateDefaults;
  readonly number: NumberDefaults;
  readonly date: DateDefaults;
};

/**
 * Default configurations for all pipes
 */
const defaults: PipeDefaults = {
  relativeTime: {
    addSuffix: true,
    maxUnits: 1,
    roundUp: false,
    largestUnit: 'year',
    smallestUnit: 'second',
    locale: 'en-US'
  },

  fileSize: {
    decimals: 2,
    binary: true,
    largestUnit: 'TiB',
    stripZeros: true,
    spaceBetween: true,
    locale: 'en-US'
  },

  truncate: {
    length: 50,
    suffix: '...',
    wordBoundary: true,
    fromStart: false,
    stripTags: true
  },

  number: {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
    useGrouping: true,
    style: 'decimal',
    locale: 'en-US'
  },

  date: {
    format: 'MM/dd/yyyy',
    showTime: false,
    use24Hour: false,
    showSeconds: false,
    locale: 'en-US'
  }
};

/**
 * Frozen default configurations
 */
export const PIPE_DEFAULTS: Readonly<PipeDefaults> = Object.freeze(defaults);