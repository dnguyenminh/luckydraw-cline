/**
 * Re-export all shared features
 */

// Export components and their types
export {
  // Components
  AlertComponent,
  AlertContainerComponent,
  EmptyStateComponent,
  LoadingSpinnerComponent,
  PageHeaderComponent,
  SpinnerComponent,
  // Collection
  SHARED_COMPONENTS
} from './components';

// Export types
export type {
  AlertType,
  AlertAction,
  AlertConfig,
  EmptyStateConfig,
  PageHeaderConfig,
  SpinnerConfig,
  ComponentPosition,
  ComponentSizeType,
  ComponentThemeType,
  BaseComponentConfig
} from './components';

// Export base classes
export {
  BaseComponent,
  LoadableComponent,
  DisableableComponent
} from './components/component.base';

// Export directives
export {
  ClickStopPropagationDirective,
  HasRoleDirective
} from './directives';

// Export pipes
export {
  RelativeTimePipe,
  FileSizePipe,
  TruncatePipe
} from './pipes';

// Export utils
export {
  generateId,
  isNullOrUndefined,
  isString,
  isNumber,
  isBoolean,
  isFunction,
  isObject,
  isArray,
  isEmpty
} from './utils';

// Export date formats
export {
  DATE_FORMATS
} from './constants';