import { Component, Input, Output, EventEmitter } from '@angular/core';

export type AlertType = 'success' | 'info' | 'warning' | 'danger';

@Component({
  selector: 'app-alert',
  template: `
    <div *ngIf="show" 
         [class]="'alert alert-' + type" 
         role="alert"
         [class.alert-dismissible]="dismissible">
      <div class="d-flex align-items-center">
        <!-- Alert icon -->
        <i *ngIf="showIcon" 
           [class]="'bi ' + getIconClass() + ' me-2'"
           [style.fontSize.rem]="1.2">
        </i>
        
        <!-- Alert content -->
        <div [innerHTML]="message"></div>
        
        <!-- Close button -->
        <button *ngIf="dismissible" 
                type="button" 
                class="btn-close ms-auto" 
                (click)="dismiss()">
        </button>
      </div>
    </div>
  `,
  styles: [`
    .alert {
      margin-bottom: 1rem;
      border: none;
      border-radius: 0.5rem;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    }

    .alert-success {
      background-color: #d4edda;
      color: #155724;
    }

    .alert-info {
      background-color: #d1ecf1;
      color: #0c5460;
    }

    .alert-warning {
      background-color: #fff3cd;
      color: #856404;
    }

    .alert-danger {
      background-color: #f8d7da;
      color: #721c24;
    }
  `]
})
export class AlertComponent {
  @Input() type: AlertType = 'info';
  @Input() message = '';
  @Input() dismissible = true;
  @Input() show = true;
  @Input() showIcon = true;
  @Output() dismissed = new EventEmitter<void>();

  getIconClass(): string {
    switch (this.type) {
      case 'success':
        return 'bi-check-circle-fill';
      case 'info':
        return 'bi-info-circle-fill';
      case 'warning':
        return 'bi-exclamation-triangle-fill';
      case 'danger':
        return 'bi-x-circle-fill';
      default:
        return 'bi-info-circle-fill';
    }
  }

  dismiss(): void {
    this.show = false;
    this.dismissed.emit();
  }
}