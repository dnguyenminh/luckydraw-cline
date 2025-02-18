import {
  isNullOrUndefined,
  isString,
  isNumber,
  isBoolean,
  isDate,
  isObject,
  isArray,
  isDict,
  isPromise,
  isFunction,
  isApiResponse,
  isPaginatedResponse,
  isFileInfo,
  isFormField,
  isErrorState,
  isThemeConfig,
  isFormData,
  isFile,
  isBlob,
  isStringArray,
  isNumberArray,
  isNonNull
} from './type-guards';

describe('Type Guards', () => {
  describe('Basic Type Guards', () => {
    it('should detect null or undefined', () => {
      expect(isNullOrUndefined(null)).toBe(true);
      expect(isNullOrUndefined(undefined)).toBe(true);
      expect(isNullOrUndefined('')).toBe(false);
    });

    it('should detect strings', () => {
      expect(isString('')).toBe(true);
      expect(isString('test')).toBe(true);
      expect(isString(123)).toBe(false);
    });

    it('should detect numbers', () => {
      expect(isNumber(123)).toBe(true);
      expect(isNumber(0)).toBe(true);
      expect(isNumber('123')).toBe(false);
    });

    it('should detect booleans', () => {
      expect(isBoolean(true)).toBe(true);
      expect(isBoolean(false)).toBe(true);
      expect(isBoolean('true')).toBe(false);
    });

    it('should detect dates', () => {
      expect(isDate(new Date())).toBe(true);
      expect(isDate(new Date('invalid'))).toBe(false);
      expect(isDate('2023-01-01')).toBe(false);
    });
  });

  describe('Complex Type Guards', () => {
    it('should detect objects', () => {
      expect(isObject({})).toBe(true);
      expect(isObject(null)).toBe(false);
      expect(isObject([])).toBe(true);
    });

    it('should detect arrays', () => {
      expect(isArray([])).toBe(true);
      expect(isArray([1, 2, 3])).toBe(true);
      expect(isArray({})).toBe(false);
    });

    it('should detect dictionaries', () => {
      expect(isDict({ key: 'value' })).toBe(true);
      expect(isDict([])).toBe(false);
      expect(isDict(null)).toBe(false);
    });

    it('should detect promises', () => {
      expect(isPromise(Promise.resolve())).toBe(true);
      expect(isPromise({})).toBe(false);
    });

    it('should detect functions', () => {
      expect(isFunction(() => {})).toBe(true);
      expect(isFunction(function() {})).toBe(true);
      expect(isFunction({})).toBe(false);
    });
  });

  describe('API Response Guards', () => {
    it('should detect API responses', () => {
      const response = {
        success: true,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: '123'
        }
      };
      expect(isApiResponse(response)).toBe(true);
      expect(isApiResponse({})).toBe(false);
    });

    it('should detect paginated responses', () => {
      const response = {
        success: true,
        meta: {
          timestamp: new Date().toISOString(),
          requestId: '123',
          page: 1,
          perPage: 10,
          total: 100,
          totalPages: 10,
          hasNext: true,
          hasPrev: false
        }
      };
      expect(isPaginatedResponse(response)).toBe(true);
      expect(isPaginatedResponse({})).toBe(false);
    });
  });

  describe('Form and File Guards', () => {
    it('should detect file info objects', () => {
      const file = {
        name: 'test.jpg',
        size: 1024,
        type: 'image/jpeg',
        lastModified: Date.now()
      };
      expect(isFileInfo(file)).toBe(true);
      expect(isFileInfo({})).toBe(false);
    });

    it('should detect form fields', () => {
      const field = {
        name: 'email',
        label: 'Email',
        value: '',
        type: 'email'
      };
      expect(isFormField(field)).toBe(true);
      expect(isFormField({})).toBe(false);
    });

    it('should detect FormData instances', () => {
      expect(isFormData(new FormData())).toBe(true);
      expect(isFormData({})).toBe(false);
    });

    it('should detect File instances', () => {
      // File constructor not available in test environment
      expect(isFile({})).toBe(false);
    });

    it('should detect Blob instances', () => {
      expect(isBlob(new Blob())).toBe(true);
      expect(isBlob({})).toBe(false);
    });
  });

  describe('Array Type Guards', () => {
    it('should detect string arrays', () => {
      expect(isStringArray(['a', 'b', 'c'])).toBe(true);
      expect(isStringArray([1, 2, 3])).toBe(false);
    });

    it('should detect number arrays', () => {
      expect(isNumberArray([1, 2, 3])).toBe(true);
      expect(isNumberArray(['1', '2', '3'])).toBe(false);
    });
  });

  describe('Utility Guards', () => {
    it('should detect non-null values', () => {
      expect(isNonNull('test')).toBe(true);
      expect(isNonNull(null)).toBe(false);
      expect(isNonNull(undefined)).toBe(false);
    });
  });
});