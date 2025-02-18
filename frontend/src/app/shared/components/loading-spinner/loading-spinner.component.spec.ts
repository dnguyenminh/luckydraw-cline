import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoadingSpinnerComponent } from './loading-spinner.component';
import { By } from '@angular/platform-browser';

describe('LoadingSpinnerComponent', () => {
  let component: LoadingSpinnerComponent;
  let fixture: ComponentFixture<LoadingSpinnerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LoadingSpinnerComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(LoadingSpinnerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should apply default size', () => {
    const spinnerElement = fixture.debugElement.query(By.css('.spinner')).nativeElement;
    expect(spinnerElement.style.width).toBe('40px');
    expect(spinnerElement.style.height).toBe('40px');
  });

  it('should apply custom size', () => {
    component.size = 60;
    fixture.detectChanges();

    const spinnerElement = fixture.debugElement.query(By.css('.spinner')).nativeElement;
    expect(spinnerElement.style.width).toBe('60px');
    expect(spinnerElement.style.height).toBe('60px');
  });

  it('should display text when provided', () => {
    component.text = 'Loading...';
    fixture.detectChanges();

    const textElement = fixture.debugElement.query(By.css('.spinner-text'));
    expect(textElement).toBeTruthy();
    expect(textElement.nativeElement.textContent).toBe('Loading...');
  });

  it('should not display text when not provided', () => {
    const textElement = fixture.debugElement.query(By.css('.spinner-text'));
    expect(textElement).toBeFalsy();
  });

  it('should apply overlay class when overlay is true', () => {
    component.overlay = true;
    fixture.detectChanges();

    const containerElement = fixture.debugElement.query(By.css('.spinner-container'));
    expect(containerElement.classes['overlay']).toBe(true);
  });

  it('should apply fullscreen class when fullscreen is true', () => {
    component.fullscreen = true;
    fixture.detectChanges();

    const containerElement = fixture.debugElement.query(By.css('.spinner-container'));
    expect(containerElement.classes['fullscreen']).toBe(true);
  });

  it('should apply custom background color', () => {
    const customColor = 'rgba(0, 0, 0, 0.5)';
    component.backgroundColor = customColor;
    fixture.detectChanges();

    const containerElement = fixture.debugElement.query(By.css('.spinner-container')).nativeElement;
    expect(containerElement.style.backgroundColor).toBe(customColor);
  });

  it('should have four ring divs for animation', () => {
    const ringDivs = fixture.debugElement.queryAll(By.css('.spinner-ring div'));
    expect(ringDivs.length).toBe(4);
  });

  it('should combine overlay and fullscreen classes', () => {
    component.overlay = true;
    component.fullscreen = true;
    fixture.detectChanges();

    const containerElement = fixture.debugElement.query(By.css('.spinner-container'));
    expect(containerElement.classes['overlay']).toBe(true);
    expect(containerElement.classes['fullscreen']).toBe(true);
  });
});