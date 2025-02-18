import { ComponentConfig } from '../component.interfaces';

/**
 * Configuration for a page header action button
 */
export interface PageHeaderAction {
  /**
   * Text label for the action
   */
  label: string;

  /**
   * Click handler for the action
   */
  onClick: () => void;

  /**
   * Optional icon name (material icon)
   */
  icon?: string;

  /**
   * Button theme
   */
  theme?: 'primary' | 'secondary' | 'danger';

  /**
   * Whether the action is disabled
   */
  disabled?: boolean;

  /**
   * Whether the action is in loading state
   */
  loading?: boolean;

  /**
   * Tooltip text
   */
  tooltip?: string;

  /**
   * Custom CSS class
   */
  className?: string;

  /**
   * Whether to show the action as a button or link
   */
  variant?: 'button' | 'link';

  /**
   * Position in the header
   */
  position?: 'left' | 'right';
}

/**
 * Configuration for a page header tab
 */
export interface PageHeaderTab {
  /**
   * Tab label
   */
  label: string;

  /**
   * Tab route path or external link
   */
  path: string;

  /**
   * Optional icon name
   */
  icon?: string;

  /**
   * Whether the tab is disabled
   */
  disabled?: boolean;

  /**
   * Badge count or text
   */
  badge?: string | number;

  /**
   * Custom CSS class
   */
  className?: string;

  /**
   * Whether to open in new tab
   */
  external?: boolean;
}

/**
 * Configuration for the page header component
 */
export interface PageHeaderConfig extends ComponentConfig {
  /**
   * Main title text
   */
  title: string;

  /**
   * Subtitle or description
   */
  subtitle?: string;

  /**
   * Optional icon name
   */
  icon?: string;

  /**
   * Action buttons
   */
  actions?: PageHeaderAction[];

  /**
   * Navigation tabs
   */
  tabs?: PageHeaderTab[];

  /**
   * Whether to show back button
   */
  showBack?: boolean;

  /**
   * Custom back URL
   */
  backUrl?: string;

  /**
   * Back button click handler
   */
  onBack?: () => void;

  /**
   * Whether to show border
   */
  border?: boolean;

  /**
   * Whether content is in loading state
   */
  loading?: boolean;

  /**
   * Custom CSS classes
   */
  containerClass?: string;
  titleClass?: string;
  subtitleClass?: string;
  actionsClass?: string;
  tabsClass?: string;
}