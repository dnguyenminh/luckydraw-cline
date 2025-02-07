import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../services/auth.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(
    private toastr: ToastrService,
    private authService: AuthService
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'An error occurred';

        if (error.error instanceof ErrorEvent) {
          // Client-side error
          errorMessage = error.error.message;
        } else {
          // Server-side error
          if (error.status === 401) {
            // Don't show error for auth errors as they're handled by auth interceptor
            return throwError(error);
          }

          if (error.error?.message) {
            errorMessage = error.error.message;
          } else {
            switch (error.status) {
              case 400:
                errorMessage = 'Bad request';
                break;
              case 403:
                errorMessage = 'Access denied';
                break;
              case 404:
                errorMessage = 'Resource not found';
                break;
              case 409:
                errorMessage = 'Resource already exists';
                break;
              case 422:
                errorMessage = 'Validation error';
                break;
              case 500:
                errorMessage = 'Server error';
                break;
              default:
                errorMessage = `Error: ${error.status}`;
                break;
            }
          }
        }

        // Don't show toasts for auth endpoints
        if (!request.url.includes('/auth/')) {
          this.toastr.error(errorMessage, 'Error', {
            timeOut: 3000,
            closeButton: true
          });
        }

        return throwError(error);
      })
    );
  }
}