import { AlertPosition } from '../components/alert/alert.types';

// Notification Types
export const NOTIFICATION_TYPES = ['success', 'error', 'warning', 'info'] as const;
export type NotificationType = typeof NOTIFICATION_TYPES[number];

// Position Options
export const NOTIFICATION_POSITIONS = [
  'top-right',
  'top-left',
  'top-center',
  'bottom-right',
  'bottom-left',
  'bottom-center'
] as const;

// Default Values
export const DEFAULT_NOTIFICATION_DURATION = 5000;
export const DEFAULT_NOTIFICATION_POSITION: AlertPosition = 'top-right';
export const MAX_NOTIFICATIONS = 5;

// Notification Configuration
export const NOTIFICATION_CONFIG = {
  duration: DEFAULT_NOTIFICATION_DURATION,
  position: DEFAULT_NOTIFICATION_POSITION,
  maxStack: MAX_NOTIFICATIONS,
  types: NOTIFICATION_TYPES,
  positions: NOTIFICATION_POSITIONS,
  animations: {
    enter: 'fade-in',
    exit: 'fade-out',
    duration: 300
  },
  icons: {
    success: 'check_circle',
    error: 'error',
    warning: 'warning',
    info: 'info'
  },
  themes: {
    success: {
      bgColor: '#d4edda',
      textColor: '#155724',
      iconColor: '#28a745'
    },
    error: {
      bgColor: '#f8d7da',
      textColor: '#721c24',
      iconColor: '#dc3545'
    },
    warning: {
      bgColor: '#fff3cd',
      textColor: '#856404',
      iconColor: '#ffc107'
    },
    info: {
      bgColor: '#cce5ff',
      textColor: '#004085',
      iconColor: '#17a2b8'
    }
  }
} as const;

// Notification Animation Classes
export const NOTIFICATION_ANIMATIONS = {
  enter: {
    'top-right': 'slide-in-right',
    'top-left': 'slide-in-left',
    'top-center': 'slide-in-down',
    'bottom-right': 'slide-in-right',
    'bottom-left': 'slide-in-left',
    'bottom-center': 'slide-in-up'
  },
  exit: {
    'top-right': 'slide-out-right',
    'top-left': 'slide-out-left',
    'top-center': 'slide-out-up',
    'bottom-right': 'slide-out-right',
    'bottom-left': 'slide-out-left',
    'bottom-center': 'slide-out-down'
  }
} as const;

// Type Guards
export function isNotificationType(value: string): value is NotificationType {
  return NOTIFICATION_TYPES.includes(value as NotificationType);
}

export function isNotificationPosition(value: string): value is AlertPosition {
  return NOTIFICATION_POSITIONS.includes(value as AlertPosition);
}