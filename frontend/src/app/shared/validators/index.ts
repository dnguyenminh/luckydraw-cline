import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

// Password validation errors type
type PasswordValidationError = {
  message: string;
  valid: boolean;
};

type PasswordValidationErrors = {
  [key: string]: PasswordValidationError;
};

/**
 * Validates required fields with custom error message
 */
export function requiredWith(errorMessage: string = 'This field is required'): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value || (typeof control.value === 'string' && !control.value.trim())) {
      return { required: errorMessage };
    }
    return null;
  };
}

/**
 * Validates email format
 */
export function emailValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;

    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(control.value) ? null : { email: 'Invalid email format' };
  };
}

/**
 * Validates password strength
 */
export function passwordValidator(options: {
  minLength?: number;
  requireUppercase?: boolean;
  requireLowercase?: boolean;
  requireNumbers?: boolean;
  requireSpecialChars?: boolean;
} = {}): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;

    const {
      minLength = 8,
      requireUppercase = true,
      requireLowercase = true,
      requireNumbers = true,
      requireSpecialChars = true
    } = options;

    const errors: PasswordValidationErrors = {};

    if (control.value.length < minLength) {
      errors['minLength'] = {
        message: `Password must be at least ${minLength} characters`,
        valid: false
      };
    }

    if (requireUppercase && !/[A-Z]/.test(control.value)) {
      errors['uppercase'] = {
        message: 'Password must contain at least one uppercase letter',
        valid: false
      };
    }

    if (requireLowercase && !/[a-z]/.test(control.value)) {
      errors['lowercase'] = {
        message: 'Password must contain at least one lowercase letter',
        valid: false
      };
    }

    if (requireNumbers && !/\d/.test(control.value)) {
      errors['numbers'] = {
        message: 'Password must contain at least one number',
        valid: false
      };
    }

    if (requireSpecialChars && !/[!@#$%^&*(),.?":{}|<>]/.test(control.value)) {
      errors['specialChars'] = {
        message: 'Password must contain at least one special character',
        valid: false
      };
    }

    return Object.keys(errors).length ? { password: errors } : null;
  };
}

/**
 * Validates matching password fields
 */
export function passwordMatchValidator(passwordField: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.parent) return null;

    const password = control.parent.get(passwordField);
    if (!password) return null;

    return control.value === password.value ? null : { 
      passwordMatch: 'Passwords do not match' 
    };
  };
}

/**
 * Validates matching fields (e.g., password confirmation)
 */
export function matchField(fieldName: string, errorMessage: string = 'Fields do not match'): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.parent) return null;

    const field = control.parent.get(fieldName);
    if (!field) return null;

    return control.value === field.value ? null : { match: errorMessage };
  };
}

/**
 * Validates phone number format
 */
export function phoneValidator(countryCode: string = ''): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;

    const phoneRegex = /^\+?[\d\s-()]{10,}$/;
    const value = countryCode + control.value.replace(/\D/g, '');
    
    return phoneRegex.test(value) ? null : { phone: 'Invalid phone number format' };
  };
}

/**
 * Validates URL format
 */
export function urlValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;

    try {
      new URL(control.value);
      return null;
    } catch {
      return { url: 'Invalid URL format' };
    }
  };
}

/**
 * Validates file size
 */
export function fileSizeValidator(maxSize: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value || !control.value[0]) return null;

    const file = control.value[0];
    return file.size <= maxSize ? null : {
      fileSize: {
        required: maxSize,
        actual: file.size
      }
    };
  };
}

/**
 * Validates file type
 */
export function fileTypeValidator(allowedTypes: string[]): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value || !control.value[0]) return null;

    const file = control.value[0];
    return allowedTypes.includes(file.type) ? null : {
      fileType: {
        allowed: allowedTypes,
        actual: file.type
      }
    };
  };
}

// Export validator types
export type ValidatorType = 'required' | 'email' | 'password' | 'passwordMatch' | 'match' | 'phone' | 'url' | 'fileSize' | 'fileType';

export interface ValidatorConfig {
  type: ValidatorType;
  message?: string;
  options?: any;
}