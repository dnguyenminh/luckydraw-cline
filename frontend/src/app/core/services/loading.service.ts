import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  private loadingSubject = new BehaviorSubject<boolean>(false);
  isLoading$ = this.loadingSubject.asObservable();
  private loadingCounter = 0;

  startLoading(): void {
    this.loadingCounter++;
    if (this.loadingCounter === 1) {
      this.loadingSubject.next(true);
    }
  }

  stopLoading(): void {
    this.loadingCounter--;
    if (this.loadingCounter === 0) {
      this.loadingSubject.next(false);
    } else if (this.loadingCounter < 0) {
      this.loadingCounter = 0;
    }
  }

  resetLoading(): void {
    this.loadingCounter = 0;
    this.loadingSubject.next(false);
  }
}