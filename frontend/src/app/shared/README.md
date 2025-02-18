# Shared Module

The shared module provides common functionality, components, directives, pipes, and utilities that can be reused across the application.

## Structure

```
shared/
├── components/           # Reusable UI components
├── constants/           # Application-wide constants
├── directives/         # Custom directives
├── interfaces/         # Common TypeScript interfaces
├── pipes/             # Custom Angular pipes
├── utils/             # Utility functions
├── validators/        # Custom form validators
├── types.ts           # Common TypeScript types
└── shared.module.ts   # Module definition
```

## Features

### Components
- `AlertComponent` - Display alert messages
- `AlertContainerComponent` - Container for alert messages
- `EmptyStateComponent` - Show empty state placeholder
- `PageHeaderComponent` - Consistent page headers
- `LoadingSpinnerComponent` - Loading indicator
- `SpinnerComponent` - Loading spinner animation

### Directives
- `ClickStopPropagationDirective` - Stop click event propagation
- `HasRoleDirective` - Role-based content visibility

### Pipes
- `RelativeTimePipe` - Format relative time (e.g., "2 hours ago")
- `FileSizePipe` - Format file sizes (e.g., "1.5 MB")
- `TruncatePipe` - Truncate long text with ellipsis

### Constants
- API configuration
- HTTP status codes
- Pagination settings
- Date formats
- File upload settings
- Notification settings
- Local storage keys
- Validation rules
- Error messages
- Theme configuration
- Application routes
- Event types
- User roles and permissions

### Utilities
- Type guards
- Date formatting
- File handling
- String manipulation
- Async utilities
- Type safety helpers

### Validators
- Required with condition
- Email format
- Password strength
- Field matching
- Phone number format
- URL format
- File size limits
- File type restrictions

## Usage

Import the SharedModule in your feature modules:

```typescript
import { NgModule } from '@angular/core';
import { SharedModule } from '@shared';

@NgModule({
  imports: [
    SharedModule
  ],
  // ...
})
export class FeatureModule { }
```

Use components:
```html
<app-page-header title="Dashboard">
  <button>Action</button>
</app-page-header>

<app-empty-state
  icon="folder"
  title="No items found"
  description="Add some items to get started">
</app-empty-state>
```

Use directives:
```html
<button clickStopPropagation>Click</button>
<div *hasRole="'ADMIN'">Admin content</div>
```

Use pipes:
```html
<span>{{ date | relativeTime }}</span>
<span>{{ fileSize | fileSize }}</span>
<span>{{ text | truncate:20 }}</span>
```

Use utilities:
```typescript
import { formatDate, isNonNull, retry } from '@shared';

const date = formatDate(new Date(), 'MM/dd/yyyy');
const items = array.filter(isNonNull);
const result = await retry(() => api.call(), 3);
```

## Contributing

When adding new features to the shared module:

1. Create the component/directive/pipe in the appropriate directory
2. Write comprehensive tests
3. Add any necessary types and interfaces
4. Update the public API in `index.ts`
5. Document the feature in this README
6. Update the `SharedModule` if needed

## Testing

Run shared module tests:
```bash
ng test --include="src/app/shared/**/*.spec.ts"
```

## Maintenance

- Keep the module focused on truly shared functionality
- Avoid business logic in shared components
- Maintain high test coverage
- Document all public APIs
- Follow consistent naming conventions
- Keep dependencies minimal