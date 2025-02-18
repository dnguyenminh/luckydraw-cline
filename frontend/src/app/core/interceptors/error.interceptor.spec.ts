import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse, HttpHandler, HttpRequest } from '@angular/common/http';
import { ErrorInterceptor } from './error.interceptor';
import { AlertService } from '@core/services/alert.service';
import { ERROR_MESSAGES } from '@shared/constants/error-messages.constants';
import { throwError } from 'rxjs';

describe('ErrorInterceptor', () => {
  let interceptor: ErrorInterceptor;
  let alertService: jasmine.SpyObj<AlertService>;
  let httpHandler: jasmine.SpyObj<HttpHandler>;

  beforeEach(() => {
    alertService = jasmine.createSpyObj('AlertService', ['error']);
    httpHandler = jasmine.createSpyObj('HttpHandler', ['handle']);

    TestBed.configureTestingModule({
      providers: [
        ErrorInterceptor,
        { provide: AlertService, useValue: alertService }
      ]
    });

    interceptor = TestBed.inject(ErrorInterceptor);
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  it('should show network error message for status 0', () => {
    const request = new HttpRequest('GET', '/test');
    const errorResponse = new HttpErrorResponse({ status: 0 });
    httpHandler.handle.and.returnValue(throwError(() => errorResponse));

    interceptor.intercept(request, httpHandler).subscribe({
      error: () => {
        expect(alertService.error).toHaveBeenCalledWith(
          ERROR_MESSAGES.NETWORK,
          jasmine.any(Object)
        );
      }
    });
  });

  it('should show unauthorized error message for status 401', () => {
    const request = new HttpRequest('GET', '/test');
    const errorResponse = new HttpErrorResponse({ status: 401 });
    httpHandler.handle.and.returnValue(throwError(() => errorResponse));

    interceptor.intercept(request, httpHandler).subscribe({
      error: () => {
        expect(alertService.error).toHaveBeenCalledWith(
          ERROR_MESSAGES.UNAUTHORIZED,
          jasmine.any(Object)
        );
      }
    });
  });

  it('should show server error message for status 500', () => {
    const request = new HttpRequest('GET', '/test');
    const errorResponse = new HttpErrorResponse({ status: 500 });
    httpHandler.handle.and.returnValue(throwError(() => errorResponse));

    interceptor.intercept(request, httpHandler).subscribe({
      error: () => {
        expect(alertService.error).toHaveBeenCalledWith(
          ERROR_MESSAGES.SERVER,
          jasmine.any(Object)
        );
      }
    });
  });

  it('should show custom error message from server', () => {
    const request = new HttpRequest('GET', '/test');
    const customMessage = 'Custom server error';
    const errorResponse = new HttpErrorResponse({
      error: { message: customMessage }
    });
    httpHandler.handle.and.returnValue(throwError(() => errorResponse));

    interceptor.intercept(request, httpHandler).subscribe({
      error: () => {
        expect(alertService.error).toHaveBeenCalledWith(
          customMessage,
          jasmine.any(Object)
        );
      }
    });
  });

  it('should show default error message for unknown errors', () => {
    const request = new HttpRequest('GET', '/test');
    const errorResponse = new HttpErrorResponse({});
    httpHandler.handle.and.returnValue(throwError(() => errorResponse));

    interceptor.intercept(request, httpHandler).subscribe({
      error: () => {
        expect(alertService.error).toHaveBeenCalledWith(
          ERROR_MESSAGES.DEFAULT,
          jasmine.any(Object)
        );
      }
    });
  });

  it('should show client error message when available', () => {
    const request = new HttpRequest('GET', '/test');
    const clientMessage = 'Client side error';
    const errorResponse = new HttpErrorResponse({
      error: new ErrorEvent('error', { message: clientMessage })
    });
    httpHandler.handle.and.returnValue(throwError(() => errorResponse));

    interceptor.intercept(request, httpHandler).subscribe({
      error: () => {
        expect(alertService.error).toHaveBeenCalledWith(
          clientMessage,
          jasmine.any(Object)
        );
      }
    });
  });
});