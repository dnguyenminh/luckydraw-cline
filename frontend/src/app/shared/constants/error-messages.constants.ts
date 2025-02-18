/**
 * Application error messages
 */
export const ERROR_MESSAGES = {
  DEFAULT: 'An error occurred. Please try again.',
  NETWORK: 'Network error. Please check your connection.',
  UNAUTHORIZED: 'You are not authorized to perform this action.',
  FORBIDDEN: 'You do not have permission to perform this action.',
  NOT_FOUND: 'The requested resource was not found.',
  SERVER: 'Server error. Please try again later.',
  VALIDATION: 'Please check your input and try again.',
  SESSION_EXPIRED: 'Your session has expired. Please log in again.',
  TIMEOUT: 'The request timed out. Please try again.',
  BAD_REQUEST: 'Invalid request. Please try again.',
  CONFLICT: 'A conflict occurred. Please try again.'
} as const;

export type ErrorMessageKey = keyof typeof ERROR_MESSAGES;