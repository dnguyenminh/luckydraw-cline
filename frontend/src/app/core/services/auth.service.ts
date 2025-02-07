import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { User, AuthResponse, LoginRequest, AuthState } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly AUTH_KEY = 'auth_state';
  private authState = new BehaviorSubject<AuthState>({
    isAuthenticated: false,
    user: null,
    token: null
  });

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.checkAuth();
  }

  login(credentials: LoginRequest): Observable<User> {
    return this.http.post<AuthResponse>('/api/auth/login', credentials).pipe(
      tap(response => {
        const state: AuthState = {
          isAuthenticated: true,
          user: response.user,
          token: response.token
        };
        this.setAuthState(state);
      }),
      map(response => response.user),
      catchError(error => {
        console.error('Login error:', error);
        throw error;
      })
    );
  }

  logout(): void {
    this.http.post('/api/auth/logout', {}).subscribe(
      () => {
        this.clearAuthState();
        this.router.navigate(['/auth/login']);
      },
      error => {
        console.error('Logout error:', error);
        // Clear state anyway
        this.clearAuthState();
        this.router.navigate(['/auth/login']);
      }
    );
  }

  checkAuth(): void {
    const savedState = localStorage.getItem(this.AUTH_KEY);
    if (savedState) {
      try {
        const state: AuthState = JSON.parse(savedState);
        this.authState.next(state);
      } catch (error) {
        console.error('Error parsing auth state:', error);
        this.clearAuthState();
      }
    }
  }

  isAuthenticated(): boolean {
    return this.authState.value.isAuthenticated;
  }

  isAdmin(): boolean {
    return this.authState.value.user?.roles.includes('ROLE_ADMIN') || false;
  }

  getCurrentUser(): User | null {
    return this.authState.value.user;
  }

  getToken(): string | null {
    return this.authState.value.token;
  }

  getAuthState(): Observable<AuthState> {
    return this.authState.asObservable();
  }

  private setAuthState(state: AuthState): void {
    localStorage.setItem(this.AUTH_KEY, JSON.stringify(state));
    this.authState.next(state);
  }

  private clearAuthState(): void {
    localStorage.removeItem(this.AUTH_KEY);
    this.authState.next({
      isAuthenticated: false,
      user: null,
      token: null
    });
  }

  refreshToken(): Observable<string> {
    return this.http.post<{ token: string }>('/api/auth/refresh-token', {}).pipe(
      tap(response => {
        const currentState = this.authState.value;
        this.setAuthState({
          ...currentState,
          token: response.token
        });
      }),
      map(response => response.token)
    );
  }

  updateUserData(user: User): void {
    const currentState = this.authState.value;
    this.setAuthState({
      ...currentState,
      user: {
        ...currentState.user,
        ...user
      }
    });
  }
}