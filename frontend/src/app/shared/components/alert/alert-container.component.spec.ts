import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AlertContainerComponent } from './alert-container.component';
import { AlertComponent } from './alert.component';
import { AlertService } from '@core/services/alert.service';
import { AlertConfig } from './alert.types';
import { BehaviorSubject } from 'rxjs';
import { CommonModule } from '@angular/common';

describe('AlertContainerComponent', () => {
  let component: AlertContainerComponent;
  let fixture: ComponentFixture<AlertContainerComponent>;
  let alertService: jasmine.SpyObj<AlertService>;
  let alertsSubject: BehaviorSubject<AlertConfig[]>;

  const mockAlerts: AlertConfig[] = [
    {
      id: '1',
      type: 'success',
      message: 'Success message',
      position: 'top-right'
    },
    {
      id: '2',
      type: 'error',
      message: 'Error message',
      position: 'top-left'
    }
  ];

  beforeEach(async () => {
    alertsSubject = new BehaviorSubject<AlertConfig[]>([]);

    const serviceSpy = jasmine.createSpyObj<AlertService>('AlertService', [
      'removeAlert',
      'success',
      'error',
      'warning',
      'info',
      'confirm',
      'clearAll',
      'clearByType',
      'getAlert',
      'getByType',
      'exists',
      'count'
    ], {
      alerts$: alertsSubject.asObservable()
    });

    await TestBed.configureTestingModule({
      imports: [CommonModule],
      declarations: [AlertContainerComponent, AlertComponent],
      providers: [
        { provide: AlertService, useValue: serviceSpy }
      ]
    }).compileComponents();

    alertService = TestBed.inject(AlertService) as jasmine.SpyObj<AlertService>;
    fixture = TestBed.createComponent(AlertContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display alerts from service', () => {
    alertsSubject.next(mockAlerts);
    fixture.detectChanges();

    const alertElements = fixture.nativeElement.querySelectorAll('app-alert');
    expect(alertElements.length).toBe(2);
  });

  it('should apply correct position classes', () => {
    alertsSubject.next(mockAlerts);
    fixture.detectChanges();

    const wrappers = fixture.nativeElement.querySelectorAll('.alert-wrapper');
    expect(wrappers[0].classList).toContain('position-top-right');
    expect(wrappers[1].classList).toContain('position-top-left');
  });

  it('should remove alert when dismissed', () => {
    alertsSubject.next(mockAlerts);
    fixture.detectChanges();

    const firstAlert = fixture.nativeElement.querySelector('app-alert');
    const dismissEvent = new CustomEvent('dismiss');
    firstAlert.dispatchEvent(dismissEvent);

    expect(alertService.removeAlert).toHaveBeenCalledWith('1');
  });

  it('should handle action clicks', () => {
    const actionSpy = jasmine.createSpy('onClick');
    const alertWithAction: AlertConfig = {
      id: '3',
      type: 'info',
      message: 'Action message',
      actions: [
        {
          label: 'Click me',
          onClick: actionSpy
        }
      ]
    };

    alertsSubject.next([alertWithAction]);
    fixture.detectChanges();

    const alertElement = fixture.nativeElement.querySelector('app-alert');
    const actionEvent = new CustomEvent('actionClick', {
      detail: { onClick: actionSpy }
    });
    alertElement.dispatchEvent(actionEvent);

    expect(actionSpy).toHaveBeenCalled();
  });

  it('should unsubscribe on destroy', () => {
    const subscription = jasmine.createSpyObj('Subscription', ['unsubscribe']);
    component['subscription'] = subscription;
    component.ngOnDestroy();
    expect(subscription.unsubscribe).toHaveBeenCalled();
  });

  it('should track alerts by id', () => {
    const alert = mockAlerts[0];
    const trackByResult = component.trackByFn(0, alert);
    expect(trackByResult).toBe(alert.id);
  });
});