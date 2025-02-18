import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { LoadingService } from './loading.service';
import { BehaviorSubject } from 'rxjs';

describe('LoadingService', () => {
  let service: LoadingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LoadingService]
    });
    service = TestBed.inject(LoadingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should start with loading state false', (done) => {
    service.loading$.subscribe(state => {
      expect(state.show).toBeFalse();
      expect(state.message).toBeUndefined();
      done();
    });
  });

  it('should show loading when started', (done) => {
    const message = 'Loading...';
    service.loading$.subscribe(state => {
      if (state.show) {
        expect(state.message).toBe(message);
        done();
      }
    });

    service.start(message);
  });

  it('should handle multiple loading requests', () => {
    const stopFirst = service.start('First');
    const stopSecond = service.start('Second');

    expect(service.activeRequests).toBe(2);

    stopFirst();
    expect(service.activeRequests).toBe(1);

    stopSecond();
    expect(service.activeRequests).toBe(0);
  });

  it('should stop all loading requests', () => {
    service.start('First');
    service.start('Second');
    
    expect(service.activeRequests).toBe(2);
    
    service.stop();
    expect(service.activeRequests).toBe(0);
  });

  it('should maintain minimum display time', fakeAsync(() => {
    const loadingStates: boolean[] = [];
    service.loading$.subscribe(state => loadingStates.push(state.show));

    const stopLoading = service.start();
    tick(100); // Before minimum display time
    stopLoading();
    tick(200); // Still before minimum display time
    
    expect(service.loadingState.show).toBeTrue();
    
    tick(300); // After minimum display time
    expect(service.loadingState.show).toBeFalse();
  }));

  it('should handle legacy show/hide methods', fakeAsync(() => {
    const loadingStates: boolean[] = [];
    service.loading$.subscribe(state => loadingStates.push(state.show));

    service.show('Loading...');
    expect(service.loadingState.show).toBeTrue();
    
    service.hide();
    tick(300);
    expect(service.loadingState.show).toBeFalse();
  }));

  it('should handle legacy startLoading/stopLoading methods', fakeAsync(() => {
    const loadingStates: boolean[] = [];
    service.loading$.subscribe(state => loadingStates.push(state.show));

    service.startLoading('Loading...');
    expect(service.loadingState.show).toBeTrue();
    
    service.stopLoading();
    tick(300);
    expect(service.loadingState.show).toBeFalse();
  }));

  it('should prevent negative request count', () => {
    const stop = service.start();
    stop(); // First stop
    stop(); // Second stop (should not make count negative)
    
    expect(service.activeRequests).toBe(0);
  });

  it('should update message with latest request', (done) => {
    service.loading$.subscribe(state => {
      if (state.show && state.message === 'Second') {
        done();
      }
    });

    service.start('First');
    service.start('Second');
  });

  it('should expose current loading state', () => {
    const stopLoading = service.start('Test');
    expect(service.loadingState.show).toBeTrue();
    expect(service.loadingState.message).toBe('Test');

    stopLoading();
    expect(service.loadingState.show).toBeFalse();
  });
});