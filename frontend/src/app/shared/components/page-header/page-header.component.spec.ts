import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { PageHeaderComponent, PageAction } from './page-header.component';
import { Location } from '@angular/common';

describe('PageHeaderComponent', () => {
  let component: PageHeaderComponent;
  let fixture: ComponentFixture<PageHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PageHeaderComponent],
      providers: [
        {
          provide: Location,
          useValue: { back: jasmine.createSpy('back') }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PageHeaderComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display title', () => {
    const testTitle = 'Test Page Title';
    component.title = testTitle;
    fixture.detectChanges();

    const titleElement = fixture.nativeElement.querySelector('h1');
    expect(titleElement.textContent.trim()).toContain(testTitle);
  });

  it('should display subtitle when provided', () => {
    const testTitle = 'Main Title';
    const testSubtitle = 'Subtitle';
    component.title = testTitle;
    component.subtitle = testSubtitle;
    fixture.detectChanges();

    const subtitleElement = fixture.nativeElement.querySelector('small');
    expect(subtitleElement.textContent.trim()).toBe(testSubtitle);
  });

  it('should display icon when provided', () => {
    const testIcon = 'bi bi-star';
    component.title = 'Test';
    component.icon = testIcon;
    fixture.detectChanges();

    const iconElement = fixture.nativeElement.querySelector('h1 i');
    expect(iconElement.classList.contains('bi-star')).toBe(true);
  });

  it('should render actions', () => {
    const actions: PageAction[] = [
      {
        label: 'Action 1',
        handler: () => {},
        type: 'primary'
      },
      {
        label: 'Action 2',
        handler: () => {},
        type: 'secondary',
        icon: 'bi bi-plus'
      }
    ];

    component.title = 'Test';
    component.actions = actions;
    fixture.detectChanges();

    const actionButtons = fixture.nativeElement.querySelectorAll('.page-header-actions button');
    expect(actionButtons.length).toBe(2);
    expect(actionButtons[0].textContent.trim()).toBe('Action 1');
    expect(actionButtons[1].textContent.trim()).toBe('Action 2');
    expect(actionButtons[1].querySelector('i').classList.contains('bi-plus')).toBe(true);
  });

  it('should handle action clicks', () => {
    const handlerSpy = jasmine.createSpy('handler');
    const actions: PageAction[] = [
      {
        label: 'Test Action',
        handler: handlerSpy,
        type: 'primary'
      }
    ];

    component.title = 'Test';
    component.actions = actions;
    fixture.detectChanges();

    const actionButton = fixture.nativeElement.querySelector('.page-header-actions button');
    actionButton.click();

    expect(handlerSpy).toHaveBeenCalled();
  });

  it('should handle back button click', () => {
    const historySpy = spyOn(window.history, 'back');
    
    component.title = 'Test';
    component.showBack = true;
    fixture.detectChanges();

    const backButton = fixture.nativeElement.querySelector('.btn-back');
    expect(backButton).toBeTruthy();

    backButton.click();
    expect(historySpy).toHaveBeenCalled();
  });

  it('should not show back button by default', () => {
    component.title = 'Test';
    fixture.detectChanges();

    const backButton = fixture.nativeElement.querySelector('.btn-back');
    expect(backButton).toBeFalsy();
  });

  it('should handle disabled actions', () => {
    const actions: PageAction[] = [
      {
        label: 'Disabled Action',
        handler: () => {},
        disabled: true
      }
    ];

    component.title = 'Test';
    component.actions = actions;
    fixture.detectChanges();

    const actionButton = fixture.nativeElement.querySelector('.page-header-actions button');
    expect(actionButton.disabled).toBe(true);
  });

  it('should apply custom classes to actions', () => {
    const actions: PageAction[] = [
      {
        label: 'Custom Action',
        handler: () => {},
        class: 'custom-class'
      }
    ];

    component.title = 'Test';
    component.actions = actions;
    fixture.detectChanges();

    const actionButton = fixture.nativeElement.querySelector('.page-header-actions button');
    expect(actionButton.classList.contains('custom-class')).toBe(true);
  });
});