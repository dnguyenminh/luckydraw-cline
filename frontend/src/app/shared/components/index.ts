/**
 * Re-export all shared components and their types
 */

// Export component interfaces and base types
export {
  ComponentSizeType,
  ComponentThemeType,
  BaseComponentConfig,
  LoadableConfig,
  DisableableConfig,
  ThemeableConfig,
  SizeableConfig,
  IconConfig,
  TooltipConfig,
  ComponentPosition
} from './interfaces';

// Export base component classes
export {
  BaseComponent,
  LoadableComponent,
  DisableableComponent,
} from './component.base';

// Import components
import { AlertComponent } from './alert/alert.component';
import { AlertContainerComponent } from './alert/alert-container.component';
import { EmptyStateComponent } from './empty-state/empty-state.component';
import { LoadingSpinnerComponent } from './loading-spinner/loading-spinner.component';
import { PageHeaderComponent } from './page-header/page-header.component';
import { SpinnerComponent } from './spinner/spinner.component';

// Re-export components
export {
  AlertComponent,
  AlertContainerComponent,
  EmptyStateComponent,
  LoadingSpinnerComponent,
  PageHeaderComponent,
  SpinnerComponent
};

// Export types
export * from './alert/alert.types';
export * from './empty-state/empty-state.types';
export * from './page-header/page-header.types';
export * from './spinner/spinner.types';

// All components collection for convenience
export const SHARED_COMPONENTS = [
  AlertComponent,
  AlertContainerComponent,
  EmptyStateComponent,
  LoadingSpinnerComponent,
  PageHeaderComponent,
  SpinnerComponent
];