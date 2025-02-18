import { Observable } from 'rxjs';
import { AlertType, AlertConfig } from '@shared/components/alert/alert.types';

export interface IAlertService {
  alerts$: Observable<AlertConfig[]>;
  
  success(message: string, config?: Partial<Omit<AlertConfig, 'type' | 'message'>>): string;
  error(message: string, config?: Partial<Omit<AlertConfig, 'type' | 'message'>>): string;
  warning(message: string, config?: Partial<Omit<AlertConfig, 'type' | 'message'>>): string;
  info(message: string, config?: Partial<Omit<AlertConfig, 'type' | 'message'>>): string;
  
  confirm(options: {
    title?: string;
    message: string;
    confirmText?: string;
    cancelText?: string;
    type?: AlertType;
  }): Promise<boolean>;
  
  removeAlert(id: string): void;
  clearAll(): void;
  clearByType(type: AlertType): void;
  getAlert(id: string): AlertConfig | undefined;
  getByType(type: AlertType): AlertConfig[];
  exists(id: string): boolean;
  count(): number;
}