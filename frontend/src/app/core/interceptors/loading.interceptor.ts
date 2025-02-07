import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { LoadingService } from '../services/loading.service';

@Injectable()
export class LoadingInterceptor implements HttpInterceptor {
  constructor(private loadingService: LoadingService) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    // Skip loading indicator for specific endpoints if needed
    if (request.url.includes('/health')) {
      return next.handle(request);
    }

    this.loadingService.startLoading();

    return next.handle(request).pipe(
      finalize(() => {
        this.loadingService.stopLoading();
      })
    );
  }
}