import { TestBed } from '@angular/core/testing';
import { SharedModule } from './shared.module';

// Components
import { AlertComponent } from './components/alert/alert.component';
import { AlertContainerComponent } from './components/alert/alert-container.component';
import { EmptyStateComponent } from './components/empty-state/empty-state.component';
import { PageHeaderComponent } from './components/page-header/page-header.component';
import { LoadingSpinnerComponent } from './components/loading-spinner/loading-spinner.component';
import { SpinnerComponent } from './components/spinner/spinner.component';

// Directives
import { ClickStopPropagationDirective } from './directives/click-stop-propagation.directive';
import { HasRoleDirective } from './directives/has-role.directive';

// Pipes
import { RelativeTimePipe } from './pipes/relative-time.pipe';
import { FileSizePipe } from './pipes/file-size.pipe';
import { TruncatePipe } from './pipes/truncate.pipe';

describe('SharedModule', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [SharedModule]
    });
  });

  it('should be created', () => {
    const module = TestBed.inject(SharedModule);
    expect(module).toBeTruthy();
  });

  describe('Components', () => {
    it('should declare and export all components', () => {
      const fixture = TestBed.createComponent(AlertComponent);
      expect(fixture.componentInstance).toBeTruthy();

      const containerFixture = TestBed.createComponent(AlertContainerComponent);
      expect(containerFixture.componentInstance).toBeTruthy();

      const emptyStateFixture = TestBed.createComponent(EmptyStateComponent);
      expect(emptyStateFixture.componentInstance).toBeTruthy();

      const pageHeaderFixture = TestBed.createComponent(PageHeaderComponent);
      expect(pageHeaderFixture.componentInstance).toBeTruthy();

      const loadingSpinnerFixture = TestBed.createComponent(LoadingSpinnerComponent);
      expect(loadingSpinnerFixture.componentInstance).toBeTruthy();

      const spinnerFixture = TestBed.createComponent(SpinnerComponent);
      expect(spinnerFixture.componentInstance).toBeTruthy();
    });
  });

  describe('Directives', () => {
    it('should declare and export all directives', () => {
      // Create test component using directives
      @Component({
        template: `
          <div clickStopPropagation></div>
          <div *hasRole="'ADMIN'"></div>
        `
      })
      class TestComponent {}

      TestBed.configureTestingModule({
        declarations: [TestComponent],
        imports: [SharedModule]
      });

      const fixture = TestBed.createComponent(TestComponent);
      expect(fixture.componentInstance).toBeTruthy();

      const clickStopElement = fixture.debugElement.query(By.directive(ClickStopPropagationDirective));
      expect(clickStopElement).toBeTruthy();

      const hasRoleElement = fixture.debugElement.query(By.directive(HasRoleDirective));
      expect(hasRoleElement).toBeTruthy();
    });
  });

  describe('Pipes', () => {
    it('should declare and export all pipes', () => {
      const relativeTimePipe = TestBed.inject(RelativeTimePipe);
      expect(relativeTimePipe).toBeTruthy();

      const fileSizePipe = TestBed.inject(FileSizePipe);
      expect(fileSizePipe).toBeTruthy();

      const truncatePipe = TestBed.inject(TruncatePipe);
      expect(truncatePipe).toBeTruthy();
    });

    it('should make pipes available in templates', () => {
      // Create test component using pipes
      @Component({
        template: `
          <div>{{ date | relativeTime }}</div>
          <div>{{ size | fileSize }}</div>
          <div>{{ text | truncate:20 }}</div>
        `
      })
      class TestComponent {
        date = new Date();
        size = 1024;
        text = 'This is a long text to truncate';
      }

      TestBed.configureTestingModule({
        declarations: [TestComponent],
        imports: [SharedModule]
      });

      const fixture = TestBed.createComponent(TestComponent);
      expect(fixture.componentInstance).toBeTruthy();
      expect(fixture.nativeElement.textContent).toBeTruthy();
    });
  });

  describe('Module Re-exports', () => {
    it('should re-export CommonModule features', () => {
      // Create test component using CommonModule features
      @Component({
        template: `
          <div *ngIf="true">Content</div>
          <div>{{ value | async }}</div>
        `
      })
      class TestComponent {
        value = Promise.resolve('test');
      }

      TestBed.configureTestingModule({
        declarations: [TestComponent],
        imports: [SharedModule]
      });

      const fixture = TestBed.createComponent(TestComponent);
      expect(fixture.componentInstance).toBeTruthy();
    });

    it('should re-export ReactiveFormsModule features', () => {
      // Create test component using ReactiveFormsModule features
      @Component({
        template: `
          <form [formGroup]="form">
            <input formControlName="test">
          </form>
        `
      })
      class TestComponent {
        form = new FormGroup({
          test: new FormControl('')
        });
      }

      TestBed.configureTestingModule({
        declarations: [TestComponent],
        imports: [SharedModule]
      });

      const fixture = TestBed.createComponent(TestComponent);
      expect(fixture.componentInstance).toBeTruthy();
    });
  });
});

// Helper Components
import { Component } from '@angular/core';
import { By } from '@angular/platform-browser';
import { FormGroup, FormControl } from '@angular/forms';