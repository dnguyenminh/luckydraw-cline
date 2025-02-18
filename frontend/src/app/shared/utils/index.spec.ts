import {
  debounce,
  throttle,
  delay,
  retry,
  memoize,
  isString,
  isNumber,
  isDate,
  isEmpty,
  formatDate,
  formatNumber,
  formatFileSize,
  capitalize,
  truncate,
  chunk,
  unique,
  groupBy,
  deepClone,
  get
} from './index';

describe('Utils', () => {
  describe('Function Utilities', () => {
    it('should debounce function calls', (done) => {
      const fn = jasmine.createSpy('fn');
      const debouncedFn = debounce(fn, 100);

      debouncedFn();
      debouncedFn();
      debouncedFn();

      expect(fn).not.toHaveBeenCalled();

      setTimeout(() => {
        expect(fn).toHaveBeenCalledTimes(1);
        done();
      }, 150);
    });

    it('should throttle function calls', () => {
      const fn = jasmine.createSpy('fn');
      const throttledFn = throttle(fn, 100);

      throttledFn();
      throttledFn();
      throttledFn();

      expect(fn).toHaveBeenCalledTimes(1);
    });

    it('should memoize function results', () => {
      const fn = jasmine.createSpy('fn').and.callFake((x: number) => x * 2);
      const memoizedFn = memoize(fn);

      expect(memoizedFn(5)).toBe(10);
      expect(memoizedFn(5)).toBe(10);
      expect(fn).toHaveBeenCalledTimes(1);
    });
  });

  describe('Async Utilities', () => {
    it('should delay execution', async () => {
      const start = Date.now();
      await delay(100);
      const duration = Date.now() - start;
      expect(duration).toBeGreaterThanOrEqual(95);
    });

    it('should retry failed operations', async () => {
      let attempts = 0;
      const fn = jasmine.createSpy('fn').and.callFake(() => {
        attempts++;
        if (attempts < 3) throw new Error('Fail');
        return 'success';
      });

      const result = await retry(fn, 3, 10);
      expect(result).toBe('success');
      expect(attempts).toBe(3);
    });
  });

  describe('Type Guards', () => {
    it('should check types correctly', () => {
      expect(isString('test')).toBe(true);
      expect(isString(123)).toBe(false);

      expect(isNumber(123)).toBe(true);
      expect(isNumber('123')).toBe(false);

      expect(isDate(new Date())).toBe(true);
      expect(isDate('2023-01-01')).toBe(false);

      expect(isEmpty([])).toBe(true);
      expect(isEmpty([1, 2, 3])).toBe(false);
      expect(isEmpty('')).toBe(true);
      expect(isEmpty('test')).toBe(false);
      expect(isEmpty({})).toBe(true);
      expect(isEmpty({ a: 1 })).toBe(false);
    });
  });

  describe('String Utilities', () => {
    it('should format dates', () => {
      const date = new Date('2023-01-01');
      expect(formatDate(date, 'yyyy-MM-dd')).toBe('2023-01-01');
    });

    it('should format numbers', () => {
      expect(formatNumber(1234.567, 2)).toBe('1,234.57');
    });

    it('should format file sizes', () => {
      expect(formatFileSize(1024)).toBe('1.00 KiB');
    });

    it('should capitalize strings', () => {
      expect(capitalize('hello')).toBe('Hello');
    });

    it('should truncate strings', () => {
      expect(truncate('long text', 4)).toBe('long...');
    });
  });

  describe('Array Utilities', () => {
    it('should chunk arrays', () => {
      const result = chunk([1, 2, 3, 4], 2);
      expect(result).toEqual([[1, 2], [3, 4]]);
    });

    it('should return unique values', () => {
      const result = unique([1, 1, 2, 2, 3]);
      expect(result).toEqual([1, 2, 3]);
    });

    it('should group by key', () => {
      interface TestItem {
        type: string;
        value: number;
      }

      const items: TestItem[] = [
        { type: 'a', value: 1 },
        { type: 'a', value: 2 },
        { type: 'b', value: 3 }
      ];

      const grouped = groupBy(items, 'type');
      expect(Object.keys(grouped)).toEqual(['a', 'b']);
      expect(grouped['a'].length).toBe(2);
      expect(grouped['b'].length).toBe(1);
    });
  });

  describe('Object Utilities', () => {
    it('should deep clone objects', () => {
      const obj = { a: 1, b: { c: 2 } };
      const clone = deepClone(obj);
      expect(clone).toEqual(obj);
      expect(clone === obj).toBe(false);
      expect(clone.b === obj.b).toBe(false);
    });

    it('should safely get nested properties', () => {
      const obj = { a: { b: { c: 1 } } };
      expect(get(obj, 'a.b.c')).toBe(1);
      expect(get(obj, 'a.b.d', 'default')).toBe('default');
      expect(get(obj, 'x.y.z')).toBeUndefined();
    });
  });
});