import { FormControl, FormGroup } from '@angular/forms';
import {
  requiredWith,
  emailValidator,
  passwordValidator,
  passwordMatchValidator,
  matchField,
  phoneValidator,
  urlValidator,
  fileSizeValidator,
  fileTypeValidator
} from './index';

describe('Form Validators', () => {
  describe('requiredWith', () => {
    const validator = requiredWith();

    it('should return error for empty value', () => {
      expect(validator(new FormControl(''))).toEqual({ required: 'This field is required' });
    });

    it('should return error for whitespace value', () => {
      expect(validator(new FormControl('   '))).toEqual({ required: 'This field is required' });
    });

    it('should return null for valid value', () => {
      expect(validator(new FormControl('test'))).toBeNull();
    });
  });

  describe('emailValidator', () => {
    const validator = emailValidator();

    it('should return null for empty value', () => {
      expect(validator(new FormControl(''))).toBeNull();
    });

    it('should return error for invalid email', () => {
      expect(validator(new FormControl('invalid'))).toEqual({ email: 'Invalid email format' });
      expect(validator(new FormControl('test@'))).toEqual({ email: 'Invalid email format' });
      expect(validator(new FormControl('test@domain'))).toEqual({ email: 'Invalid email format' });
    });

    it('should return null for valid email', () => {
      expect(validator(new FormControl('test@domain.com'))).toBeNull();
    });
  });

  describe('passwordValidator', () => {
    const validator = passwordValidator();

    it('should return null for empty value', () => {
      expect(validator(new FormControl(''))).toBeNull();
    });

    it('should validate minimum length', () => {
      const result = validator(new FormControl('short'));
      expect(result?.['password']['minLength'].valid).toBeFalse();
    });

    it('should validate uppercase requirement', () => {
      const result = validator(new FormControl('nocapitalletters123!'));
      expect(result?.['password']['uppercase'].valid).toBeFalse();
    });

    it('should validate lowercase requirement', () => {
      const result = validator(new FormControl('NOCAPITALLETTERS123!'));
      expect(result?.['password']['lowercase'].valid).toBeFalse();
    });

    it('should validate number requirement', () => {
      const result = validator(new FormControl('NoNumbers!'));
      expect(result?.['password']['numbers'].valid).toBeFalse();
    });

    it('should validate special character requirement', () => {
      const result = validator(new FormControl('NoSpecialChars123'));
      expect(result?.['password']['specialChars'].valid).toBeFalse();
    });

    it('should return null for valid password', () => {
      expect(validator(new FormControl('ValidP@ssw0rd'))).toBeNull();
    });
  });

  describe('passwordMatchValidator', () => {
    let form: FormGroup;

    beforeEach(() => {
      form = new FormGroup({
        password: new FormControl(''),
        confirmPassword: new FormControl('')
      });
    });

    it('should return null when passwords match', () => {
      form.patchValue({ password: 'test123', confirmPassword: 'test123' });
      const validator = passwordMatchValidator('password');
      expect(validator(form.get('confirmPassword')!)).toBeNull();
    });

    it('should return error when passwords do not match', () => {
      form.patchValue({ password: 'test123', confirmPassword: 'different' });
      const validator = passwordMatchValidator('password');
      expect(validator(form.get('confirmPassword')!)).toEqual({
        passwordMatch: 'Passwords do not match'
      });
    });
  });

  describe('phoneValidator', () => {
    const validator = phoneValidator();

    it('should return null for empty value', () => {
      expect(validator(new FormControl(''))).toBeNull();
    });

    it('should return error for invalid phone number', () => {
      expect(validator(new FormControl('abc'))).toEqual({ phone: 'Invalid phone number format' });
    });

    it('should return null for valid phone number', () => {
      expect(validator(new FormControl('1234567890'))).toBeNull();
      expect(validator(new FormControl('+1-234-567-8900'))).toBeNull();
    });
  });

  describe('urlValidator', () => {
    const validator = urlValidator();

    it('should return null for empty value', () => {
      expect(validator(new FormControl(''))).toBeNull();
    });

    it('should return error for invalid URL', () => {
      expect(validator(new FormControl('not-a-url'))).toEqual({ url: 'Invalid URL format' });
    });

    it('should return null for valid URL', () => {
      expect(validator(new FormControl('https://example.com'))).toBeNull();
    });
  });

  describe('fileSizeValidator', () => {
    const maxSize = 1024; // 1KB
    const validator = fileSizeValidator(maxSize);

    it('should return null for empty value', () => {
      expect(validator(new FormControl(null))).toBeNull();
    });

    it('should return error for file exceeding max size', () => {
      const file = new File([''], 'test.txt');
      Object.defineProperty(file, 'size', { value: maxSize + 1 });
      expect(validator(new FormControl([file]))).toEqual({
        fileSize: { required: maxSize, actual: maxSize + 1 }
      });
    });

    it('should return null for valid file size', () => {
      const file = new File([''], 'test.txt');
      Object.defineProperty(file, 'size', { value: maxSize - 1 });
      expect(validator(new FormControl([file]))).toBeNull();
    });
  });

  describe('fileTypeValidator', () => {
    const allowedTypes = ['image/jpeg', 'image/png'];
    const validator = fileTypeValidator(allowedTypes);

    it('should return null for empty value', () => {
      expect(validator(new FormControl(null))).toBeNull();
    });

    it('should return error for invalid file type', () => {
      const file = new File([''], 'test.txt', { type: 'text/plain' });
      expect(validator(new FormControl([file]))).toEqual({
        fileType: { allowed: allowedTypes, actual: 'text/plain' }
      });
    });

    it('should return null for valid file type', () => {
      const file = new File([''], 'test.jpg', { type: 'image/jpeg' });
      expect(validator(new FormControl([file]))).toBeNull();
    });
  });
});