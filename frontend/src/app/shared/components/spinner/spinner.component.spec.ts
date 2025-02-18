import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SpinnerComponent } from './spinner.component';
import { By } from '@angular/platform-browser';

describe('SpinnerComponent', () => {
  let component: SpinnerComponent;
  let fixture: ComponentFixture<SpinnerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SpinnerComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(SpinnerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render medium size spinner by default', () => {
    const spinnerElement = fixture.debugElement.query(By.css('.spinner')).nativeElement;
    expect(spinnerElement.style.width).toBe('24px');
    expect(spinnerElement.style.height).toBe('24px');
    expect(spinnerElement.classList.contains('spinner-sm')).toBeFalse();
    expect(spinnerElement.classList.contains('spinner-lg')).toBeFalse();
  });

  it('should apply small size class', () => {
    component.size = 'small';
    fixture.detectChanges();

    const spinnerElement = fixture.debugElement.query(By.css('.spinner')).nativeElement;
    expect(spinnerElement.style.width).toBe('16px');
    expect(spinnerElement.style.height).toBe('16px');
    expect(spinnerElement.classList.contains('spinner-sm')).toBeTrue();
  });

  it('should apply large size class', () => {
    component.size = 'large';
    fixture.detectChanges();

    const spinnerElement = fixture.debugElement.query(By.css('.spinner')).nativeElement;
    expect(spinnerElement.style.width).toBe('32px');
    expect(spinnerElement.style.height).toBe('32px');
    expect(spinnerElement.classList.contains('spinner-lg')).toBeTrue();
  });

  it('should apply custom color', () => {
    const customColor = 'rgb(255, 0, 0)';
    component.color = customColor;
    fixture.detectChanges();

    const spinnerElement = fixture.debugElement.query(By.css('.spinner')).nativeElement;
    expect(spinnerElement.style.borderTopColor).toBe(customColor);
  });

  it('should use default primary color', () => {
    const spinnerElement = fixture.debugElement.query(By.css('.spinner')).nativeElement;
    expect(spinnerElement.style.borderTopColor).toBe('var(--bs-primary)');
  });

  it('should return correct size in pixels', () => {
    expect(component.sizeInPixels).toBe(24); // Default medium

    component.size = 'small';
    expect(component.sizeInPixels).toBe(16);

    component.size = 'large';
    expect(component.sizeInPixels).toBe(32);
  });

  it('should handle invalid size input', () => {
    // @ts-ignore: Testing invalid input
    component.size = 'invalid';
    expect(component.sizeInPixels).toBe(24); // Should default to medium
  });
});