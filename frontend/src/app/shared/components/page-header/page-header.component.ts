import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-page-header',
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.scss']
})
export class PageHeaderComponent {
  @Input() title = '';
  @Input() subtitle = '';
  @Input() showBackButton = false;
  @Input() showCreateButton = false;
  @Input() createButtonText = 'Create New';
  @Input() createButtonIcon = 'bi-plus-lg';
  @Input() actions: { label: string; icon?: string; onClick: () => void }[] = [];
  @Input() breadcrumbs: { label: string; link?: string }[] = [];

  @Output() createClick = new EventEmitter<void>();
  @Output() backClick = new EventEmitter<void>();

  onCreateClick(): void {
    this.createClick.emit();
  }

  onBackClick(): void {
    this.backClick.emit();
  }

  trackByIndex(index: number): number {
    return index;
  }
}