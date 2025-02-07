import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading-spinner',
  template: `
    <div class="spinner-container" [class.overlay]="overlay">
      <div class="spinner-border" 
           [class]="size ? 'spinner-border-' + size : ''"
           [style.color]="color"
           role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <div *ngIf="message" class="spinner-message mt-2">
        {{ message }}
      </div>
    </div>
  `,
  styles: [`
    .spinner-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 100px;
    }

    .spinner-container.overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(255, 255, 255, 0.8);
      z-index: 9999;
    }

    .spinner-message {
      color: var(--primary-color);
      font-size: 0.875rem;
    }

    .spinner-border-sm {
      width: 1rem;
      height: 1rem;
    }

    .spinner-border-lg {
      width: 3rem;
      height: 3rem;
    }
  `]
})
export class LoadingSpinnerComponent {
  @Input() overlay = false;
  @Input() message = '';
  @Input() size: 'sm' | 'lg' | '' = '';
  @Input() color = 'var(--primary-color)';
}