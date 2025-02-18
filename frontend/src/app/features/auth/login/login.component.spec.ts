import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '@core/services/auth.service';
import { AlertService } from '@core/services/alert.service';
import { LoadingService } from '@core/services/loading.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let alertService: jasmine.SpyObj<AlertService>;
  let loadingService: jasmine.SpyObj<LoadingService>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['login', 'isAuthenticated']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const alertServiceSpy = jasmine.createSpyObj('AlertService', ['success', 'error']);
    const loadingServiceSpy = jasmine.createSpyObj('LoadingService', ['show', 'hide']);

    await TestBed.configureTestingModule({
      declarations: [ LoginComponent ],
      imports: [ ReactiveFormsModule ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: AlertService, useValue: alertServiceSpy },
        { provide: LoadingService, useValue: loadingServiceSpy }
      ]
    }).compileComponents();

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    alertService = TestBed.inject(AlertService) as jasmine.SpyObj<AlertService>;
    loadingService = TestBed.inject(LoadingService) as jasmine.SpyObj<LoadingService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty form', () => {
    expect(component.loginForm.get('username')?.value).toBe('');
    expect(component.loginForm.get('password')?.value).toBe('');
    expect(component.loginForm.get('rememberMe')?.value).toBe(false);
  });

  it('should validate required fields', () => {
    component.onSubmit();
    expect(component.loginForm.get('username')?.errors?.['required']).toBeTruthy();
    expect(component.loginForm.get('password')?.errors?.['required']).toBeTruthy();
  });

  it('should validate username minimum length', () => {
    component.loginForm.patchValue({ username: 'ab' });
    expect(component.loginForm.get('username')?.errors?.['minlength']).toBeTruthy();
  });

  it('should validate password minimum length', () => {
    component.loginForm.patchValue({ password: '12345' });
    expect(component.loginForm.get('password')?.errors?.['minlength']).toBeTruthy();
  });

  it('should call auth service on valid form submission', () => {
    const credentials = {
      username: 'testuser',
      password: 'password123',
      rememberMe: true
    };

    authService.login.and.returnValue(of({ accessToken: 'token', user: {} } as any));
    
    component.loginForm.patchValue(credentials);
    component.onSubmit();

    expect(loadingService.show).toHaveBeenCalled();
    expect(authService.login).toHaveBeenCalledWith(credentials);
    expect(router.navigate).toHaveBeenCalledWith(['/']);
    expect(alertService.success).toHaveBeenCalledWith('Login successful');
    expect(loadingService.hide).toHaveBeenCalled();
  });

  it('should handle login error', () => {
    const error = { error: { message: 'Invalid credentials' } };
    authService.login.and.returnValue(throwError(() => error));

    component.loginForm.patchValue({
      username: 'testuser',
      password: 'wrong'
    });
    component.onSubmit();

    expect(alertService.error).toHaveBeenCalledWith('Invalid credentials');
    expect(loadingService.hide).toHaveBeenCalled();
  });

  it('should redirect if already authenticated', () => {
    authService.isAuthenticated.and.returnValue(true);
    component.ngOnInit();
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });
});