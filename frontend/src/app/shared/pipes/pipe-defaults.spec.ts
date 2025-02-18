import {
  PIPE_DEFAULTS,
  type PipeDefaults,
  type RelativeTimeDefaults,
  type FileSizeDefaults,
  type TruncateDefaults
} from './pipe-defaults';

describe('Pipe Defaults', () => {
  describe('relativeTime defaults', () => {
    it('should have correct relative time defaults', () => {
      const defaults = PIPE_DEFAULTS.relativeTime;

      expect(defaults.addSuffix).toBeDefined();
      expect(defaults.maxUnits).toBeDefined();
      expect(defaults.roundUp).toBeDefined();
      expect(defaults.largestUnit).toBe('year');
      expect(defaults.smallestUnit).toBe('second');
      expect(defaults.locale).toBe('en-US');
    });
  });

  describe('fileSize defaults', () => {
    it('should have correct file size defaults', () => {
      const defaults = PIPE_DEFAULTS.fileSize;

      expect(defaults.decimals).toBe(2);
      expect(defaults.binary).toBe(true);
      expect(defaults.largestUnit).toBe('TiB');
      expect(defaults.stripZeros).toBe(true);
      expect(defaults.spaceBetween).toBe(true);
      expect(defaults.locale).toBe('en-US');
    });
  });

  describe('truncate defaults', () => {
    it('should have correct truncate defaults', () => {
      const defaults = PIPE_DEFAULTS.truncate;

      expect(defaults.length).toBe(50);
      expect(defaults.suffix).toBe('...');
      expect(defaults.wordBoundary).toBe(true);
      expect(defaults.fromStart).toBe(false);
      expect(defaults.stripTags).toBe(true);
    });
  });

  describe('number defaults', () => {
    it('should have correct number defaults', () => {
      const defaults = PIPE_DEFAULTS.number;

      expect(defaults.minimumFractionDigits).toBe(0);
      expect(defaults.maximumFractionDigits).toBe(2);
      expect(defaults.useGrouping).toBe(true);
      expect(defaults.style).toBe('decimal');
      expect(defaults.locale).toBe('en-US');
    });
  });

  describe('date defaults', () => {
    it('should have correct date defaults', () => {
      const defaults = PIPE_DEFAULTS.date;

      expect(defaults.format).toBe('MM/dd/yyyy');
      expect(defaults.showTime).toBe(false);
      expect(defaults.use24Hour).toBe(false);
      expect(defaults.showSeconds).toBe(false);
      expect(defaults.locale).toBe('en-US');
    });
  });

  describe('Type Safety', () => {
    it('should be type-safe with PipeDefaults type', () => {
      const config: PipeDefaults = PIPE_DEFAULTS;
      
      // Verify each config section exists
      expect(config.relativeTime).toBeDefined();
      expect(config.fileSize).toBeDefined();
      expect(config.truncate).toBeDefined();
      expect(config.number).toBeDefined();
      expect(config.date).toBeDefined();

      // TypeScript compilation check - these assignments should compile
      const relativeTimeConfig: RelativeTimeDefaults = {
        addSuffix: true,
        maxUnits: 1,
        roundUp: false,
        largestUnit: 'year',
        smallestUnit: 'second',
        locale: 'en-US'
      };

      const fileSizeConfig: FileSizeDefaults = {
        decimals: 2,
        binary: true,
        largestUnit: 'TiB',
        stripZeros: true,
        spaceBetween: true,
        locale: 'en-US'
      };

      const truncateConfig: TruncateDefaults = {
        length: 50,
        suffix: '...',
        wordBoundary: true,
        fromStart: false,
        stripTags: true
      };

      // Verify types match
      expect(typeof relativeTimeConfig.addSuffix).toBe('boolean');
      expect(typeof fileSizeConfig.decimals).toBe('number');
      expect(typeof truncateConfig.length).toBe('number');
    });
  });

  describe('Object Immutability', () => {
    it('should be frozen and immutable', () => {
      expect(Object.isFrozen(PIPE_DEFAULTS)).toBe(true);
      
      // Attempting to modify should either throw or be ignored
      const attemptModification = () => {
        // @ts-ignore - Intentionally trying to modify frozen object
        PIPE_DEFAULTS.relativeTime.addSuffix = false;
      };
      
      expect(attemptModification).toThrow();
      expect(PIPE_DEFAULTS.relativeTime.addSuffix).toBe(true);
    });

    it('should have frozen nested objects', () => {
      expect(Object.isFrozen(PIPE_DEFAULTS.relativeTime)).toBe(true);
      expect(Object.isFrozen(PIPE_DEFAULTS.fileSize)).toBe(true);
      expect(Object.isFrozen(PIPE_DEFAULTS.truncate)).toBe(true);
      expect(Object.isFrozen(PIPE_DEFAULTS.number)).toBe(true);
      expect(Object.isFrozen(PIPE_DEFAULTS.date)).toBe(true);
    });
  });
});