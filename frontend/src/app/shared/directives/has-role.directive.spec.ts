import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BehaviorSubject } from 'rxjs';
import { AuthService } from '@core/services/auth.service';
import { HasRoleDirective } from './has-role.directive';

@Component({
  template: `
    <div *hasRole="'ROLE_ADMIN'">Admin content</div>
    <div *hasRole="['ROLE_ADMIN', 'ROLE_MANAGER']">Admin or manager content</div>
  `
})
class TestComponent {}

describe('HasRoleDirective', () => {
  let component: TestComponent;
  let fixture: ComponentFixture<TestComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let authStateSubject: BehaviorSubject<boolean>;

  beforeEach(async () => {
    authStateSubject = new BehaviorSubject<boolean>(false);

    authService = jasmine.createSpyObj('AuthService', ['hasRole'], {
      isAuthenticated$: authStateSubject.asObservable()
    });

    await TestBed.configureTestingModule({
      declarations: [
        TestComponent,
        HasRoleDirective
      ],
      providers: [
        { provide: AuthService, useValue: authService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should hide content when not authenticated', () => {
    authStateSubject.next(false);
    fixture.detectChanges();

    const elements = fixture.debugElement.queryAll(By.css('div'));
    expect(elements.length).toBe(0);
  });

  it('should show admin content when user has admin role', () => {
    authStateSubject.next(true);
    authService.hasRole.and.returnValue(true);
    fixture.detectChanges();

    const elements = fixture.debugElement.queryAll(By.css('div'));
    expect(elements.length).toBe(2);
    expect(elements[0].nativeElement.textContent).toContain('Admin content');
  });

  it('should hide content when user does not have required role', () => {
    authStateSubject.next(true);
    authService.hasRole.and.returnValue(false);
    fixture.detectChanges();

    const elements = fixture.debugElement.queryAll(By.css('div'));
    expect(elements.length).toBe(0);
  });

  it('should handle multiple roles correctly', () => {
    authStateSubject.next(true);
    authService.hasRole.and.callFake((roles: string[]) => 
      roles.includes('ROLE_MANAGER')
    );
    fixture.detectChanges();

    const elements = fixture.debugElement.queryAll(By.css('div'));
    expect(elements.length).toBe(1);
    expect(elements[0].nativeElement.textContent).toContain('Admin or manager content');
  });

  it('should update view when authentication state changes', () => {
    // Initially not authenticated
    authStateSubject.next(false);
    fixture.detectChanges();
    expect(fixture.debugElement.queryAll(By.css('div')).length).toBe(0);

    // Become authenticated with correct role
    authStateSubject.next(true);
    authService.hasRole.and.returnValue(true);
    fixture.detectChanges();
    expect(fixture.debugElement.queryAll(By.css('div')).length).toBe(2);

    // Become unauthenticated again
    authStateSubject.next(false);
    fixture.detectChanges();
    expect(fixture.debugElement.queryAll(By.css('div')).length).toBe(0);
  });

  it('should clean up subscriptions on destroy', () => {
    const directive = fixture.debugElement
      .queryAllNodes(By.directive(HasRoleDirective))[0]
      .injector.get(HasRoleDirective);

    spyOn(authStateSubject, 'unsubscribe');
    fixture.destroy();
    
    expect(directive['destroy$'].closed).toBeTrue();
  });
});