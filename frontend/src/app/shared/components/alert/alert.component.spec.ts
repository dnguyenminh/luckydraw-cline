import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { AlertComponent } from './alert.component';
import { AlertType } from './alert.types';
import { DEFAULT_NOTIFICATION_DURATION } from '../../constants/notification.constants';

describe('AlertComponent', () => {
  let component: AlertComponent;
  let fixture: ComponentFixture<AlertComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AlertComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(AlertComponent);
    component = fixture.componentInstance;
    component.message = 'Test message';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display message', () => {
    const messageEl = fixture.nativeElement.querySelector('.alert-text');
    expect(messageEl.textContent).toContain('Test message');
  });

  it('should display title when provided', () => {
    component.title = 'Test Title';
    fixture.detectChanges();
    const titleEl = fixture.nativeElement.querySelector('.alert-title');
    expect(titleEl.textContent).toContain('Test Title');
  });

  it('should have correct type class', () => {
    component.type = 'success';
    fixture.detectChanges();
    const alertEl = fixture.nativeElement.querySelector('.alert');
    expect(alertEl.classList).toContain('alert-success');
  });

  it('should be dismissible by default', () => {
    const closeBtn = fixture.nativeElement.querySelector('.btn-close');
    expect(closeBtn).toBeTruthy();
  });

  it('should hide close button when dismissible is false', () => {
    component.dismissible = false;
    fixture.detectChanges();
    const closeBtn = fixture.nativeElement.querySelector('.btn-close');
    expect(closeBtn).toBeFalsy();
  });

  it('should emit dismiss event when close button is clicked', () => {
    const spy = spyOn(component.dismiss, 'emit');
    const closeBtn = fixture.nativeElement.querySelector('.btn-close');
    closeBtn.click();
    expect(spy).toHaveBeenCalled();
    expect(component.visible).toBeFalse();
    expect(component.closed).toBeTrue();
  });

  it('should display actions when provided', () => {
    const actionSpy = jasmine.createSpy('onClick');
    component.actions = [
      {
        label: 'Action 1',
        onClick: actionSpy,
        theme: 'success'
      },
      {
        label: 'Action 2',
        onClick: actionSpy,
        theme: 'error'
      }
    ];
    fixture.detectChanges();
    const actionButtons = fixture.nativeElement.querySelectorAll('.alert-actions button');
    expect(actionButtons.length).toBe(2);
    expect(actionButtons[0].textContent).toContain('Action 1');
    expect(actionButtons[1].textContent).toContain('Action 2');
  });

  it('should call action onClick when clicked', () => {
    const actionSpy = jasmine.createSpy('onClick');
    component.actions = [
      {
        label: 'Action',
        onClick: actionSpy,
        theme: 'success'
      }
    ];
    fixture.detectChanges();
    const actionButton = fixture.nativeElement.querySelector('.alert-actions button');
    actionButton.click();
    expect(actionSpy).toHaveBeenCalled();
  });

  it('should auto dismiss after timeout', fakeAsync(() => {
    component.timeout = 1000;
    const spy = spyOn(component.dismiss, 'emit');
    fixture.detectChanges();
    tick(1000);
    expect(spy).toHaveBeenCalled();
    expect(component.visible).toBeFalse();
    expect(component.closed).toBeTrue();
  }));

  it('should use default timeout if not provided', () => {
    expect(component.timeout).toBe(DEFAULT_NOTIFICATION_DURATION);
  });

  it('should clear timeout on destroy', fakeAsync(() => {
    component.timeout = 1000;
    fixture.detectChanges();
    const spy = spyOn(component.dismiss, 'emit');
    fixture.destroy();
    tick(1000);
    expect(spy).not.toHaveBeenCalled();
  }));
});