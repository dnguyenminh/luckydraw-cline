import { TestBed } from '@angular/core/testing';
import { AlertService } from './alert.service';
import { AlertConfig } from '@shared/components/alert/alert.types';
import { DEFAULT_NOTIFICATION_DURATION } from '@shared/constants/notification.constants';

describe('AlertService', () => {
  let service: AlertService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AlertService]
    });
    service = TestBed.inject(AlertService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should show alert with correct configuration', (done) => {
    service.alerts$.subscribe(alerts => {
      if (alerts.length > 0) {
        expect(alerts[0]).toEqual(
          jasmine.objectContaining({
            type: 'success',
            message: 'Test message',
            timeout: DEFAULT_NOTIFICATION_DURATION
          })
        );
        done();
      }
    });

    service.success('Test message');
  });

  it('should show alert with custom configuration', (done) => {
    const customConfig = {
      timeout: 1000,
      dismissible: false,
      position: 'top-left' as const
    };

    service.alerts$.subscribe(alerts => {
      if (alerts.length > 0) {
        expect(alerts[0]).toEqual(
          jasmine.objectContaining({
            type: 'error',
            message: 'Test message',
            ...customConfig
          })
        );
        done();
      }
    });

    service.error('Test message', customConfig);
  });

  it('should remove alert by id', () => {
    const id = service.success('Test message');
    service.removeAlert(id);
    expect(service.exists(id)).toBeFalse();
  });

  it('should clear all alerts', () => {
    service.success('Test 1');
    service.error('Test 2');
    service.warning('Test 3');
    
    service.clearAll();
    
    expect(service.count()).toBe(0);
  });

  it('should clear alerts by type', () => {
    service.success('Success 1');
    service.success('Success 2');
    service.error('Error 1');
    
    service.clearByType('success');
    
    const remainingAlerts = service.getByType('success');
    expect(remainingAlerts.length).toBe(0);
    expect(service.count()).toBe(1);
  });

  it('should limit maximum number of alerts', () => {
    for (let i = 0; i < 10; i++) {
      service.info(`Message ${i}`);
    }
    
    expect(service.count()).toBe(5); // maxAlerts is 5
  });

  it('should show confirmation dialog', async () => {
    const confirmOptions = {
      title: 'Confirm',
      message: 'Are you sure?',
      confirmText: 'Yes',
      cancelText: 'No'
    };

    // Simulate confirm action
    setTimeout(() => {
      const alert = service.getByType('info')[0];
      alert.actions?.[0].onClick();
    });

    const result = await service.confirm(confirmOptions);
    expect(result).toBeTrue();
  });

  it('should handle confirmation cancellation', async () => {
    const confirmOptions = {
      message: 'Are you sure?'
    };

    // Simulate cancel action
    setTimeout(() => {
      const alert = service.getByType('info')[0];
      alert.actions?.[1].onClick();
    });

    const result = await service.confirm(confirmOptions);
    expect(result).toBeFalse();
  });

  it('should return alert by id', () => {
    const id = service.success('Test message');
    const alert = service.getAlert(id);
    expect(alert?.message).toBe('Test message');
  });

  it('should check if alert exists', () => {
    const id = service.success('Test message');
    expect(service.exists(id)).toBeTrue();
    expect(service.exists('non-existent')).toBeFalse();
  });
});