import { Injectable } from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  Router,
  UrlTree
} from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastrService } from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    if (this.authService.isAuthenticated() && this.authService.isAdmin()) {
      return true;
    }

    // If not admin, show error and redirect to dashboard
    if (this.authService.isAuthenticated()) {
      this.toastr.error('Access denied. Admin privileges required.', 'Error');
      return this.router.createUrlTree(['/dashboard']);
    }

    // If not authenticated, redirect to login
    return this.router.createUrlTree(['/auth/login'], {
      queryParams: {
        returnUrl: state.url
      }
    });
  }
}