import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiUrlService {
  private apiUrl: string;

  constructor() {
    this.apiUrl = environment.apiUrl;
  }

  getUrl(): string {
    return this.apiUrl.endsWith('/') ? this.apiUrl.slice(0, -1) : this.apiUrl;
  }

  getFullUrl(path: string): string {
    const baseUrl = this.getUrl();
    const cleanPath = path.startsWith('/') ? path.slice(1) : path;
    return `${baseUrl}/${cleanPath}`;
  }

  // Event-specific URLs
  getEventsUrl(): string {
    return `${this.getUrl()}/api/events`;
  }

  getEventUrl(id: number): string {
    return `${this.getEventsUrl()}/${id}`;
  }

  getAdminEventsUrl(): string {
    return `${this.getUrl()}/api/admin/events`;
  }

  getAdminEventUrl(id: number): string {
    return `${this.getAdminEventsUrl()}/${id}`;
  }

  // Event participant URLs
  getEventParticipantsUrl(eventId: number): string {
    return `${this.getEventUrl(eventId)}/participants`;
  }

  getEventDrawHistoryUrl(eventId: number): string {
    return `${this.getEventUrl(eventId)}/draw-history`;
  }

  // Event action URLs
  getEventDrawUrl(eventId: number): string {
    return `${this.getEventUrl(eventId)}/draw`;
  }

  getEventStartUrl(eventId: number): string {
    return `${this.getEventUrl(eventId)}/start`;
  }

  getEventCompleteUrl(eventId: number): string {
    return `${this.getEventUrl(eventId)}/complete`;
  }

  getEventCancelUrl(eventId: number): string {
    return `${this.getEventUrl(eventId)}/cancel`;
  }
}