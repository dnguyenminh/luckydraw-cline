import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  templateUrl: './empty-state.component.html',
  styleUrls: ['./empty-state.component.scss']
})
export class EmptyStateComponent {
  @Input() icon = 'bi-inbox';
  @Input() title = 'No Data Available';
  @Input() message = 'There are no items to display at this time.';
  @Input() actionLabel?: string;
  @Input() actionIcon?: string;
  @Input() theme: 'light' | 'dark' = 'light';
  @Output() actionClick = new EventEmitter<void>();

  @Input() set showAction(value: boolean) {
    this._showAction = value;
    if (!this.actionLabel) {
      this.actionLabel = 'Add New';
    }
    if (!this.actionIcon) {
      this.actionIcon = 'bi-plus-lg';
    }
  }
  get showAction(): boolean {
    return this._showAction;
  }

  private _showAction = false;

  get containerClass(): string {
    return `empty-state ${this.theme === 'dark' ? 'theme-dark' : ''}`;
  }
}