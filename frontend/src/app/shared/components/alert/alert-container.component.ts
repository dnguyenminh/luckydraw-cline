import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { AlertService, AlertConfig } from '@core/services/alert.service';
import { fadeInOut } from '@shared/animations/fade.animation';

@Component({
  selector: 'app-alert-container',
  template: `
    <div class="alert-container">
      <div *ngFor="let alert of alerts; let i = index"
           class="alert-wrapper"
           [ngClass]="'position-' + (alert.position || 'top-right')"
           [@fadeInOut]>
        <app-alert
          [type]="alert.type"
          [message]="alert.message"
          [title]="alert.title"
          [icon]="alert.icon"
          [dismissible]="alert.dismissible || false"
          [className]="alert.className"
          (dismiss)="onDismiss(alert)">
        </app-alert>
      </div>
    </div>
  `,
  styles: [`
    .alert-container {
      position: fixed;
      z-index: 1050;
      pointer-events: none;
      width: 100%;
      height: 100%;
    }
    .alert-wrapper {
      position: fixed;
      max-width: 350px;
      padding: 1rem;
      pointer-events: auto;
    }
    .position-top-right { top: 0; right: 0; }
    .position-top-left { top: 0; left: 0; }
    .position-bottom-right { bottom: 0; right: 0; }
    .position-bottom-left { bottom: 0; left: 0; }
  `],
  animations: [fadeInOut]
})
export class AlertContainerComponent implements OnInit, OnDestroy {
  alerts: AlertConfig[] = [];
  private subscription?: Subscription;

  constructor(private alertService: AlertService) {}

  ngOnInit(): void {
    this.subscription = this.alertService.alerts$.subscribe(
      alerts => this.alerts = alerts
    );
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  onDismiss(alert: AlertConfig): void {
    this.alertService.removeAlert(alert.id);
  }
}