// Core directives
export * from './click-stop-propagation.directive';
export * from './has-role.directive';

// Interfaces for directive configuration
export interface DirectiveConfig {
  /**
   * Custom CSS classes to apply
   */
  class?: string;

  /**
   * Whether the directive is disabled
   */
  disabled?: boolean;
}

/**
 * Role-based configuration for HasRoleDirective
 */
export interface RoleConfig extends DirectiveConfig {
  /**
   * Required roles (any of these roles will grant access)
   */
  roles: string[];

  /**
   * All of these roles are required (if specified)
   */
  requireAll?: boolean;

  /**
   * Whether to hide element when access is denied
   * (otherwise disables it)
   */
  hideOnDeny?: boolean;

  /**
   * Optional message to show when access is denied
   */
  denyMessage?: string;
}

/**
 * Configuration for tracking directive
 */
export interface TrackingConfig extends DirectiveConfig {
  /**
   * Event category
   */
  category: string;

  /**
   * Event action
   */
  action: string;

  /**
   * Event label (optional)
   */
  label?: string;

  /**
   * Event value (optional)
   */
  value?: number;

  /**
   * Additional properties (optional)
   */
  properties?: { [key: string]: any };
}

/**
 * Configuration for tooltip directive
 */
export interface TooltipConfig extends DirectiveConfig {
  /**
   * Tooltip text
   */
  text: string;

  /**
   * Tooltip placement
   */
  placement?: 'top' | 'right' | 'bottom' | 'left';

  /**
   * Delay before showing tooltip (ms)
   */
  showDelay?: number;

  /**
   * Delay before hiding tooltip (ms)
   */
  hideDelay?: number;
}

/**
 * Configuration for debounce directive
 */
export interface DebounceConfig extends DirectiveConfig {
  /**
   * Debounce delay in milliseconds
   */
  delay?: number;

  /**
   * Whether to trigger on the leading edge
   */
  leading?: boolean;

  /**
   * Whether to trigger on the trailing edge
   */
  trailing?: boolean;
}

/**
 * Configuration for drag and drop directive
 */
export interface DragDropConfig extends DirectiveConfig {
  /**
   * Data to transfer
   */
  dragData?: any;

  /**
   * Allowed drop zones
   */
  dropZones?: string[];

  /**
   * CSS class to apply while dragging
   */
  dragClass?: string;

  /**
   * CSS class to apply when draggable
   */
  dragHandle?: string;

  /**
   * Whether to disable drag on mobile
   */
  disableOnMobile?: boolean;
}

// Directive event types
export type DirectiveEvent<T = any> = {
  type: string;
  data?: T;
  originalEvent?: Event;
};