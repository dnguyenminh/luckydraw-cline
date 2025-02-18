import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { AlertType } from '@shared/components/alert/alert.component';

export interface AlertConfig {
  id: string;
  type: AlertType;
  message: string;
  title?: string;
  icon?: string;
  dismissible?: boolean;
  timeout?: number;
  position?: 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left';
  className?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  private alertsSubject = new BehaviorSubject<AlertConfig[]>([]);
  public alerts$: Observable<AlertConfig[]> = this.alertsSubject.asObservable();

  success(message: string, title?: string): void {
    this.alert({ type: 'success', message, title });
  }

  error(message: string, title?: string): void {
    this.alert({ type: 'error', message, title });
  }

  info(message: string, title?: string): void {
    this.alert({ type: 'info', message, title });
  }

  warning(message: string, title?: string): void {
    this.alert({ type: 'warning', message, title });
  }

  private alert(config: Partial<AlertConfig>): void {
    const id = Date.now().toString();
    const alert: AlertConfig = {
      id,
      type: 'info',
      message: '',
      dismissible: true,
      timeout: 5000,
      position: 'top-right',
      ...config
    };

    this.alertsSubject.next([...this.alertsSubject.value, alert]);

    if (alert.timeout) {
      setTimeout(() => this.removeAlert(id), alert.timeout);
    }
  }

  removeAlert(id: string): void {
    this.alertsSubject.next(
      this.alertsSubject.value.filter(alert => alert.id !== id)
    );
  }
}