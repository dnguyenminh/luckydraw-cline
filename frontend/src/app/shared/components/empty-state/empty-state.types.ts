import { ComponentConfig } from '../component.interfaces';

/**
 * Configuration for an empty state action button
 */
export interface EmptyStateAction {
  label: string;
  onClick: () => void;
  theme?: 'primary' | 'secondary';
  icon?: string;
  disabled?: boolean;
  loading?: boolean;
}

/**
 * Configuration for the empty state component
 */
export interface EmptyStateConfig extends ComponentConfig {
  /**
   * Title text to display
   */
  title: string;

  /**
   * Description text to display
   */
  description?: string;

  /**
   * Icon to display (material icon name)
   */
  icon?: string;

  /**
   * Image source URL to display instead of an icon
   */
  image?: string;

  /**
   * Image alt text when using an image
   */
  imageAlt?: string;

  /**
   * List of action buttons to display
   */
  actions?: EmptyStateAction[];

  /**
   * Whether to show an animated placeholder
   */
  animated?: boolean;

  /**
   * Custom CSS class for the container
   */
  containerClass?: string;

  /**
   * Custom CSS class for the icon/image
   */
  mediaClass?: string;

  /**
   * Custom CSS class for the content
   */
  contentClass?: string;

  /**
   * Custom CSS class for the actions
   */
  actionsClass?: string;
}

/**
 * Position of the empty state content
 */
export type EmptyStatePosition = 'center' | 'top' | 'bottom';