import { TestBed } from '@angular/core/testing';
import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { LoadingInterceptor } from './loading.interceptor';
import { LoadingService } from '../services/loading.service';

describe('LoadingInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let loadingService: LoadingService;
  let interceptor: LoadingInterceptor;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        LoadingService,
        LoadingInterceptor,
        {
          provide: HTTP_INTERCEPTORS,
          useClass: LoadingInterceptor,
          multi: true
        }
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    loadingService = TestBed.inject(LoadingService);
    interceptor = TestBed.inject(LoadingInterceptor);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  it('should show loading on HTTP request', () => {
    const startSpy = spyOn(loadingService, 'start').and.callThrough();

    httpClient.get('/api/test').subscribe();
    httpMock.expectOne('/api/test').flush({});

    expect(startSpy).toHaveBeenCalled();
  });

  it('should not show loading for excluded URLs', () => {
    const startSpy = spyOn(loadingService, 'start').and.callThrough();

    httpClient.get('/api/health').subscribe();
    httpMock.expectOne('/api/health').flush({});

    expect(startSpy).not.toHaveBeenCalled();
  });

  it('should handle multiple concurrent requests', () => {
    const startSpy = spyOn(loadingService, 'start').and.callThrough();

    // Make three concurrent requests
    httpClient.get('/api/test1').subscribe();
    httpClient.get('/api/test2').subscribe();
    httpClient.get('/api/test3').subscribe();

    httpMock.expectOne('/api/test1').flush({});
    httpMock.expectOne('/api/test2').flush({});
    httpMock.expectOne('/api/test3').flush({});

    expect(startSpy).toHaveBeenCalledTimes(3);
  });

  it('should handle request errors', () => {
    const startSpy = spyOn(loadingService, 'start').and.callThrough();

    httpClient.get('/api/error').subscribe({
      error: () => {}
    });

    httpMock.expectOne('/api/error').error(new ErrorEvent('Network error'));

    expect(startSpy).toHaveBeenCalled();
  });

  describe('URL exclusion management', () => {
    it('should add excluded URLs', () => {
      interceptor.addExcludedUrls('/api/custom');

      httpClient.get('/api/custom').subscribe();
      httpMock.expectOne('/api/custom').flush({});

      expect(loadingService.activeRequests).toBe(0);
    });

    it('should remove excluded URLs', () => {
      interceptor.addExcludedUrls('/api/temp');
      interceptor.removeExcludedUrls('/api/temp');

      const startSpy = spyOn(loadingService, 'start').and.callThrough();

      httpClient.get('/api/temp').subscribe();
      httpMock.expectOne('/api/temp').flush({});

      expect(startSpy).toHaveBeenCalled();
    });

    it('should clear all excluded URLs', () => {
      interceptor.clearExcludedUrls();

      const startSpy = spyOn(loadingService, 'start').and.callThrough();

      httpClient.get('/api/health').subscribe();
      httpMock.expectOne('/api/health').flush({});

      expect(startSpy).toHaveBeenCalled();
    });

    it('should get excluded URLs', () => {
      const urls = interceptor.getExcludedUrls();
      expect(urls).toContain('/api/health');
      expect(urls).toContain('/api/metrics');
      expect(urls).toContain('/api/websocket');
    });
  });
});