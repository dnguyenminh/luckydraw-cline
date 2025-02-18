import { Component, DebugElement } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ClickStopPropagationDirective } from './click-stop-propagation.directive';

@Component({
  template: `
    <div (click)="onOuterClick()" class="outer">
      <div (click)="onInnerClick()" class="inner">
        <button [clickStopPropagation] (click)="onButtonClick()">Click Me</button>
      </div>
    </div>
  `
})
class TestComponent {
  outerClicked = false;
  innerClicked = false;
  buttonClicked = false;

  onOuterClick(): void {
    this.outerClicked = true;
  }

  onInnerClick(): void {
    this.innerClicked = true;
  }

  onButtonClick(): void {
    this.buttonClicked = true;
  }
}

describe('ClickStopPropagationDirective', () => {
  let component: TestComponent;
  let fixture: ComponentFixture<TestComponent>;
  let button: DebugElement;
  let inner: DebugElement;
  let outer: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        TestComponent,
        ClickStopPropagationDirective
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    button = fixture.debugElement.query(By.css('button'));
    inner = fixture.debugElement.query(By.css('.inner'));
    outer = fixture.debugElement.query(By.css('.outer'));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should stop click event propagation', () => {
    button.nativeElement.click();
    expect(component.buttonClicked).toBe(true);
    expect(component.innerClicked).toBe(false);
    expect(component.outerClicked).toBe(false);
  });

  it('should allow normal click propagation without directive', () => {
    inner.nativeElement.click();
    expect(component.innerClicked).toBe(true);
    expect(component.outerClicked).toBe(true);
  });

  it('should stop mousedown event propagation', () => {
    const event = new MouseEvent('mousedown');
    button.nativeElement.dispatchEvent(event);
    expect(event.defaultPrevented).toBeFalsy();
    expect(component.innerClicked).toBe(false);
    expect(component.outerClicked).toBe(false);
  });

  it('should stop mouseup event propagation', () => {
    const event = new MouseEvent('mouseup');
    button.nativeElement.dispatchEvent(event);
    expect(event.defaultPrevented).toBeFalsy();
    expect(component.innerClicked).toBe(false);
    expect(component.outerClicked).toBe(false);
  });

  it('should stop double click event propagation', () => {
    const event = new MouseEvent('dblclick');
    button.nativeElement.dispatchEvent(event);
    expect(event.defaultPrevented).toBeFalsy();
    expect(component.innerClicked).toBe(false);
    expect(component.outerClicked).toBe(false);
  });
});