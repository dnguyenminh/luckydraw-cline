import { TruncatePipe } from './truncate.pipe';
import { TruncateConfig } from './interfaces';
import { PIPE_DEFAULTS } from './index';

describe('TruncatePipe', () => {
  let pipe: TruncatePipe;

  beforeEach(() => {
    pipe = new TruncatePipe();
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  describe('Input Validation', () => {
    it('should handle invalid inputs', () => {
      expect(pipe.transform('')).toBe('');
      // @ts-ignore - Testing null handling
      expect(pipe.transform(null)).toBe('');
      // @ts-ignore - Testing undefined handling
      expect(pipe.transform(undefined)).toBe('');
    });
  });

  describe('Basic Truncation', () => {
    const longText = 'This is a very long text that needs to be truncated';

    it('should truncate text at specified length', () => {
      const config: Partial<TruncateConfig> = { length: 20 };
      expect(pipe.transform(longText, config).length).toBe(23); // 20 + '...'
    });

    it('should not truncate text shorter than length', () => {
      const shortText = 'Short text';
      const config: Partial<TruncateConfig> = { length: 20 };
      expect(pipe.transform(shortText, config)).toBe(shortText);
    });
  });

  describe('Configuration Options', () => {
    const text = 'This is a sample text for testing truncation';

    it('should respect custom suffix', () => {
      const config: Partial<TruncateConfig> = {
        length: 10,
        suffix: '---'
      };
      const result = pipe.transform(text, config);
      expect(result.endsWith('---')).toBe(true);
    });

    it('should handle word boundary option', () => {
      const config: Partial<TruncateConfig> = {
        length: 15,
        wordBoundary: true
      };
      const result = pipe.transform(text, config);
      expect(result.charAt(result.length - 4)).toBe(' '); // Space before suffix
    });

    it('should handle fromStart option', () => {
      const config: Partial<TruncateConfig> = {
        length: 20,
        fromStart: true
      };
      const result = pipe.transform(text, config);
      expect(result.startsWith('...')).toBe(true);
    });

    it('should handle stripTags option', () => {
      const htmlText = '<p>This is <strong>bold</strong> text</p>';
      const config: Partial<TruncateConfig> = {
        length: 10,
        stripTags: true
      };
      const result = pipe.transform(htmlText, config);
      expect(result).not.toContain('<');
      expect(result).not.toContain('>');
    });
  });

  describe('HTML Handling', () => {
    const htmlText = '<p>This is <strong>bold</strong> and <em>italic</em> text</p>';

    it('should preserve HTML when stripTags is false', () => {
      const config: Partial<TruncateConfig> = {
        length: 20,
        stripTags: false
      };
      const result = pipe.preserveHtml(htmlText, config);
      expect(result).toContain('<');
      expect(result).toContain('>');
    });

    it('should get correct visible length', () => {
      const visibleLength = pipe.getVisibleLength(htmlText);
      expect(visibleLength).toBe('This is bold and italic text'.length);
    });

    it('should detect truncation need correctly', () => {
      expect(pipe.needsTruncation(htmlText, 10, true)).toBe(true);
      expect(pipe.needsTruncation(htmlText, 100, true)).toBe(false);
    });

    it('should calculate remaining length correctly', () => {
      const config: Partial<TruncateConfig> = { length: 10 };
      const remaining = pipe.getRemainingLength(htmlText, config);
      expect(remaining).toBeGreaterThan(0);
    });
  });

  describe('Word Boundary Handling', () => {
    it('should find word boundaries correctly', () => {
      const text = 'word1 word2 word3';
      const boundary = pipe.findWordBoundary(text, 8);
      expect(text.charAt(boundary)).toBe(' ');
    });

    it('should return original position if no word boundary found', () => {
      const text = 'word1word2word3';
      const position = 5;
      expect(pipe.findWordBoundary(text, position)).toBe(position);
    });
  });

  describe('Default Configuration', () => {
    it('should use default configuration', () => {
      const text = 'Sample text for testing defaults';
      const result = pipe.transform(text);
      expect(result).toBe(pipe.transform(text, PIPE_DEFAULTS.truncate));
    });
  });
});