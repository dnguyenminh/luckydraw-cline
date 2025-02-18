import { RelativeTimePipe } from './relative-time.pipe';
import { RelativeTimeConfig } from './interfaces';
import { PIPE_DEFAULTS } from './index';

describe('RelativeTimePipe', () => {
  let pipe: RelativeTimePipe;

  beforeEach(() => {
    pipe = new RelativeTimePipe();
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should handle null input', () => {
    expect(pipe.transform('')).toBe('');
  });

  it('should handle invalid date', () => {
    expect(pipe.transform('invalid-date')).toBe('');
  });

  describe('Time Units', () => {
    it('should format seconds ago', () => {
      const date = new Date(Date.now() - 10000); // 10 seconds ago
      expect(pipe.transform(date, { addSuffix: true })).toContain('seconds ago');
    });

    it('should format minutes ago', () => {
      const date = new Date(Date.now() - 120000); // 2 minutes ago
      expect(pipe.transform(date, { addSuffix: true })).toContain('minutes ago');
    });

    it('should format hours ago', () => {
      const date = new Date(Date.now() - 7200000); // 2 hours ago
      expect(pipe.transform(date, { addSuffix: true })).toContain('hours ago');
    });

    it('should format days ago', () => {
      const date = new Date(Date.now() - 172800000); // 2 days ago
      expect(pipe.transform(date, { addSuffix: true })).toContain('days ago');
    });

    it('should format months ago', () => {
      const date = new Date(Date.now() - 5256000000); // 2 months ago
      expect(pipe.transform(date, { addSuffix: true })).toContain('months ago');
    });

    it('should format years ago', () => {
      const date = new Date(Date.now() - 63072000000); // 2 years ago
      expect(pipe.transform(date, { addSuffix: true })).toContain('years ago');
    });
  });

  describe('Configuration Options', () => {
    const date = new Date(Date.now() - 5000); // 5 seconds ago

    it('should respect addSuffix option', () => {
      const withSuffix = pipe.transform(date, { addSuffix: true });
      const withoutSuffix = pipe.transform(date, { addSuffix: false });
      
      expect(withSuffix).toContain('ago');
      expect(withoutSuffix).not.toContain('ago');
    });

    it('should respect locale option', () => {
      const defaultLocale = pipe.transform(date, { addSuffix: true });
      const frenchLocale = pipe.transform(date, { locale: 'fr', addSuffix: true });
      
      expect(defaultLocale).not.toBe(frenchLocale);
    });

    it('should respect roundUp option', () => {
      const roundedDown = pipe.transform(date, { roundUp: false, addSuffix: true });
      const roundedUp = pipe.transform(date, { roundUp: true, addSuffix: true });
      
      expect(roundedDown).not.toBe(roundedUp);
    });

    it('should respect largestUnit option', () => {
      const config: Partial<RelativeTimeConfig> = {
        largestUnit: 'minute',
        addSuffix: true
      };
      expect(pipe.transform(date, config)).toContain('minute');
    });

    it('should respect smallestUnit option', () => {
      const config: Partial<RelativeTimeConfig> = {
        smallestUnit: 'second',
        addSuffix: true
      };
      expect(pipe.transform(date, config)).toContain('second');
    });
  });

  describe('Utility Methods', () => {
    it('should get time units', () => {
      const units = pipe.getUnits();
      expect(units.length).toBeGreaterThan(0);
      expect(units[0].unit).toBeDefined();
      expect(units[0].ms).toBeDefined();
      expect(typeof units[0].unit).toBe('string');
      expect(typeof units[0].ms).toBe('number');
    });

    it('should parse date correctly', () => {
      const now = new Date();
      expect(pipe.parseDate(now).getTime()).toBe(now.getTime());
      expect(pipe.parseDate(now.getTime()).getTime()).toBe(now.getTime());
      expect(pipe.parseDate(now.toISOString()).getTime()).toBe(now.getTime());
    });

    it('should validate dates', () => {
      expect(pipe.isValidDate(new Date())).toBe(true);
      expect(pipe.isValidDate(new Date('invalid'))).toBe(false);
    });

    it('should format time difference', () => {
      const from = new Date(Date.now() - 5000); // 5 seconds ago
      const result = pipe.formatDiff(from);
      expect(result).toContain('seconds ago');
    });
  });

  describe('Default Configuration', () => {
    it('should use default configuration', () => {
      const date = new Date(Date.now() - 5000);
      const result = pipe.transform(date);
      expect(result).toBe(pipe.transform(date, PIPE_DEFAULTS.relativeTime));
    });
  });
});