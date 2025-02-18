import { NotificationType } from '@shared/constants/notification.constants';

export type AlertPosition = 'top-right' | 'top-left' | 'top-center' | 
                          'bottom-right' | 'bottom-left' | 'bottom-center';

export type AlertType = NotificationType;

export interface AlertAction {
  label: string;
  onClick: () => void;
  theme?: AlertType;
  disabled?: boolean;
  icon?: string;
}

export interface AlertConfig {
  id: string;
  type: AlertType;
  message: string;
  title?: string;
  icon?: string;
  dismissible?: boolean;
  timeout?: number;
  position?: AlertPosition;
  actions?: AlertAction[];
  className?: string;
}

export interface AlertOptions extends Partial<Omit<AlertConfig, 'id' | 'type' | 'message'>> {
  // Any additional alert options can be added here
}

export type AlertConfigWithoutId = Omit<AlertConfig, 'id'>;

/**
 * Alert theme configuration for styling
 */
export const AlertThemes = {
  success: {
    icon: 'check_circle',
    bgColor: '#d4edda',
    textColor: '#155724',
    iconColor: '#28a745'
  },
  error: {
    icon: 'error',
    bgColor: '#f8d7da',
    textColor: '#721c24',
    iconColor: '#dc3545'
  },
  warning: {
    icon: 'warning',
    bgColor: '#fff3cd',
    textColor: '#856404',
    iconColor: '#ffc107'
  },
  info: {
    icon: 'info',
    bgColor: '#cce5ff',
    textColor: '#004085',
    iconColor: '#17a2b8'
  }
} as const;

export type AlertThemeType = keyof typeof AlertThemes;