import { FileSizePipe } from './file-size.pipe';
import { FileSizeConfig, BinaryUnit, DecimalUnit } from './interfaces';
import { PIPE_DEFAULTS } from './index';

describe('FileSizePipe', () => {
  let pipe: FileSizePipe;

  beforeEach(() => {
    pipe = new FileSizePipe();
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  describe('Input Validation', () => {
    it('should handle invalid inputs', () => {
      expect(pipe.transform(0)).toBe('0 B');
      expect(pipe.transform(NaN)).toBe('');
      // @ts-ignore - Testing null handling
      expect(pipe.transform(null)).toBe('');
      // @ts-ignore - Testing undefined handling
      expect(pipe.transform(undefined)).toBe('');
    });
  });

  describe('Binary Units', () => {
    it('should format bytes', () => {
      const config: Partial<FileSizeConfig> = { binary: true };
      expect(pipe.transform(1024, config)).toBe('1 KiB');
      expect(pipe.transform(1024 * 1024, config)).toBe('1 MiB');
      expect(pipe.transform(1024 * 1024 * 1024, config)).toBe('1 GiB');
    });

    it('should handle decimals correctly', () => {
      const bytes = 1536; // 1.5 KiB
      expect(pipe.transform(bytes, { decimals: 3 })).toBe('1.500 KiB');
      expect(pipe.transform(bytes, { decimals: 1 })).toBe('1.5 KiB');
      expect(pipe.transform(bytes, { decimals: 0 })).toBe('2 KiB');
    });
  });

  describe('Decimal Units', () => {
    const config: Partial<FileSizeConfig> = { binary: false };

    it('should format bytes', () => {
      expect(pipe.transform(1000, config)).toBe('1 KB');
      expect(pipe.transform(1000000, config)).toBe('1 MB');
      expect(pipe.transform(1000000000, config)).toBe('1 GB');
    });
  });

  describe('Configuration Options', () => {
    const bytes = 1024;

    it('should respect spaceBetween option', () => {
      expect(pipe.transform(bytes, { spaceBetween: true })).toBe('1 KiB');
      expect(pipe.transform(bytes, { spaceBetween: false })).toBe('1KiB');
    });

    it('should respect stripZeros option', () => {
      const config: Partial<FileSizeConfig> = { decimals: 2 };
      expect(pipe.transform(1024, { ...config, stripZeros: true })).toBe('1 KiB');
      expect(pipe.transform(1024, { ...config, stripZeros: false })).toBe('1.00 KiB');
    });

    it('should respect largestUnit option', () => {
      const largeNumber = 1024 * 1024 * 1024; // 1 GiB
      const configMiB: Partial<FileSizeConfig> = { binary: true, largestUnit: 'MiB' };
      const configGiB: Partial<FileSizeConfig> = { binary: true, largestUnit: 'GiB' };
      
      expect(pipe.transform(largeNumber, configMiB)).toContain('1024 MiB');
      expect(pipe.transform(largeNumber, configGiB)).toContain('1 GiB');
    });
  });

  describe('Utility Methods', () => {
    it('should get units', () => {
      expect(pipe.getUnits(true)).toContain('KiB' as BinaryUnit);
      expect(pipe.getUnits(false)).toContain('KB' as DecimalUnit);
    });

    it('should convert to bytes', () => {
      expect(pipe.toBytes('1 KiB')).toBe(1024);
      expect(pipe.toBytes('1 KB', false)).toBe(1000);
      expect(pipe.toBytes('1.5 MiB')).toBe(1.5 * 1024 * 1024);
    });

    it('should throw error for invalid format', () => {
      expect(() => pipe.toBytes('invalid')).toThrow();
      expect(() => pipe.toBytes('1 InvalidUnit')).toThrow();
    });

    it('should get appropriate unit', () => {
      expect(pipe.getUnit(1024)).toBe('KiB' as BinaryUnit);
      expect(pipe.getUnit(1024 * 1024)).toBe('MiB' as BinaryUnit);
      expect(pipe.getUnit(1000, false)).toBe('KB' as DecimalUnit);
    });

    it('should validate file size strings', () => {
      expect(pipe.isValidFileSize('1 KiB')).toBe(true);
      expect(pipe.isValidFileSize('1.5 MB')).toBe(true);
      expect(pipe.isValidFileSize('invalid')).toBe(false);
    });
  });

  describe('Default Configuration', () => {
    it('should use default configuration', () => {
      const bytes = 1024;
      const result = pipe.transform(bytes);
      expect(result).toBe(pipe.transform(bytes, PIPE_DEFAULTS.fileSize));
    });
  });
});