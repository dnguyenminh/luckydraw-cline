import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EmptyStateComponent } from './empty-state.component';

describe('EmptyStateComponent', () => {
  let component: EmptyStateComponent;
  let fixture: ComponentFixture<EmptyStateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EmptyStateComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(EmptyStateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display default values', () => {
    const element = fixture.nativeElement;
    
    expect(element.querySelector('.empty-state-icon i').classList.contains('bi-inbox')).toBe(true);
    expect(element.querySelector('.empty-state-title').textContent).toContain('No Data Available');
    expect(element.querySelector('.empty-state-message')).toBeFalsy();
    expect(element.querySelector('.empty-state-actions')).toBeFalsy();
  });

  it('should display custom values', () => {
    component.icon = 'bi bi-search';
    component.title = 'Custom Title';
    component.message = 'Custom Message';
    fixture.detectChanges();

    const element = fixture.nativeElement;
    
    expect(element.querySelector('.empty-state-icon i').classList.contains('bi-search')).toBe(true);
    expect(element.querySelector('.empty-state-title').textContent).toContain('Custom Title');
    expect(element.querySelector('.empty-state-message').textContent).toContain('Custom Message');
  });

  it('should apply large class when large input is true', () => {
    component.large = true;
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.empty-state-lg')).toBeTruthy();
  });

  it('should handle primary action', () => {
    const handlerSpy = jasmine.createSpy('handler');
    component.primaryAction = {
      label: 'Primary Action',
      handler: handlerSpy
    };
    fixture.detectChanges();

    const primaryButton = fixture.nativeElement.querySelector('.btn-primary');
    expect(primaryButton.textContent.trim()).toBe('Primary Action');

    primaryButton.click();
    expect(handlerSpy).toHaveBeenCalled();
  });

  it('should handle secondary action', () => {
    const handlerSpy = jasmine.createSpy('handler');
    component.secondaryAction = {
      label: 'Secondary Action',
      handler: handlerSpy
    };
    fixture.detectChanges();

    const secondaryButton = fixture.nativeElement.querySelector('.btn-secondary');
    expect(secondaryButton.textContent.trim()).toBe('Secondary Action');

    secondaryButton.click();
    expect(handlerSpy).toHaveBeenCalled();
  });

  it('should display both actions when provided', () => {
    component.primaryAction = {
      label: 'Primary',
      handler: () => {}
    };
    component.secondaryAction = {
      label: 'Secondary',
      handler: () => {}
    };
    fixture.detectChanges();

    const actions = fixture.nativeElement.querySelector('.empty-state-actions');
    const buttons = actions.querySelectorAll('button');
    
    expect(buttons.length).toBe(2);
    expect(buttons[0].textContent.trim()).toBe('Primary');
    expect(buttons[1].textContent.trim()).toBe('Secondary');
  });

  it('should not display actions section when no actions provided', () => {
    component.primaryAction = undefined;
    component.secondaryAction = undefined;
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.empty-state-actions')).toBeFalsy();
  });
});