/**
 * Common interfaces for components
 */

export type ComponentSizeType = 'xs' | 'sm' | 'md' | 'lg' | 'xl';

export type ComponentThemeType = 
  | 'primary'
  | 'secondary'
  | 'success'
  | 'danger'
  | 'warning'
  | 'info'
  | 'light'
  | 'dark';

export interface BaseComponentConfig {
  size?: ComponentSizeType;
  theme?: ComponentThemeType;
  disabled?: boolean;
  loading?: boolean;
  className?: string;
  testId?: string;
  [key: string]: any;
}

export interface LoadableConfig extends BaseComponentConfig {
  loading?: boolean;
  loadingText?: string;
}

export interface DisableableConfig extends BaseComponentConfig {
  disabled?: boolean;
  disabledReason?: string;
}

export interface ThemeableConfig extends BaseComponentConfig {
  theme?: ComponentThemeType;
  outline?: boolean;
}

export interface SizeableConfig extends BaseComponentConfig {
  size?: ComponentSizeType;
  fluid?: boolean;
}

export interface IconConfig extends BaseComponentConfig {
  icon?: string;
  iconPosition?: 'left' | 'right';
  iconSize?: ComponentSizeType;
  iconClassName?: string;
}

export interface TooltipConfig extends BaseComponentConfig {
  tooltip?: string;
  tooltipPlacement?: 'top' | 'right' | 'bottom' | 'left';
  tooltipDelay?: number;
}

export type ComponentPosition =
  | 'top-left'
  | 'top-right'
  | 'top-center'
  | 'bottom-left'
  | 'bottom-right'
  | 'bottom-center'
  | 'center';