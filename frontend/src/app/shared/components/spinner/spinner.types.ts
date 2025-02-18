import { ComponentConfig } from '../component.interfaces';

/**
 * Spinner size options
 */
export type SpinnerSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl';

/**
 * Spinner theme options
 */
export type SpinnerTheme = 
  | 'primary'
  | 'secondary'
  | 'success'
  | 'danger'
  | 'warning'
  | 'info'
  | 'light'
  | 'dark';

/**
 * Configuration for the spinner component
 */
export interface SpinnerConfig extends ComponentConfig {
  /**
   * Component size
   */
  size?: SpinnerSize;

  /**
   * Color theme
   */
  theme?: SpinnerTheme;

  /**
   * Whether to show spinner inline with text
   */
  inline?: boolean;

  /**
   * Loading text to display
   */
  text?: string;

  /**
   * Text position relative to spinner
   */
  textPosition?: 'left' | 'right' | 'top' | 'bottom';

  /**
   * Whether to center the spinner in its container
   */
  centered?: boolean;

  /**
   * Whether to show fullscreen overlay
   */
  fullscreen?: boolean;

  /**
   * Whether to show with transparent backdrop
   */
  transparent?: boolean;

  /**
   * Custom spinner animation speed (ms)
   */
  speed?: number;

  /**
   * Custom spinner thickness
   */
  thickness?: number;

  /**
   * Custom diameter size in pixels
   */
  diameter?: number;

  /**
   * Custom CSS classes
   */
  containerClass?: string;
  spinnerClass?: string;
  textClass?: string;

  /**
   * z-index for fullscreen mode
   */
  zIndex?: number;

  /**
   * Whether to show spinner with delay
   */
  delay?: number;

  /**
   * Whether to show with minimum display time
   */
  minDuration?: number;
}

/**
 * Default spinner configuration
 */
export const DEFAULT_SPINNER_CONFIG: SpinnerConfig = {
  size: 'md',
  theme: 'primary',
  inline: false,
  centered: false,
  fullscreen: false,
  transparent: false,
  speed: 750,
  thickness: 2,
  delay: 200,
  minDuration: 300,
  zIndex: 9999,
  textPosition: 'bottom'
};