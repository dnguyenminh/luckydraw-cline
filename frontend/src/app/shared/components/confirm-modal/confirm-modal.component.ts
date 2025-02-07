import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

type ButtonType = 'primary' | 'secondary' | 'danger' | 'warning';

@Component({
  selector: 'app-confirm-modal',
  template: `
    <div class="modal-header">
      <h5 class="modal-title">{{ title }}</h5>
      <button type="button" class="btn-close" (click)="modal.dismiss()"></button>
    </div>
    <div class="modal-body">
      {{ message }}
    </div>
    <div class="modal-footer">
      <button type="button"
              class="btn btn-secondary"
              (click)="modal.dismiss()">
        {{ cancelText }}
      </button>
      <button type="button"
              class="btn"
              [class]="getConfirmButtonClass()"
              (click)="modal.close(true)">
        {{ confirmText }}
      </button>
    </div>
  `,
  styles: [`
    .modal-header {
      border-bottom: 1px solid #dee2e6;
      padding: 1rem;
    }

    .modal-body {
      padding: 1rem;
    }

    .modal-footer {
      border-top: 1px solid #dee2e6;
      padding: 1rem;
    }

    .btn {
      min-width: 80px;
    }
  `]
})
export class ConfirmModalComponent {
  @Input() title = 'Confirm Action';
  @Input() message = 'Are you sure you want to proceed?';
  @Input() confirmText = 'Confirm';
  @Input() cancelText = 'Cancel';
  @Input() type: ButtonType = 'primary';

  constructor(public modal: NgbActiveModal) {}

  getConfirmButtonClass(): string {
    return `btn-${this.type}`;
  }
}