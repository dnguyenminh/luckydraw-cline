import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event, EventListResponse } from '../models/event.model';
import { ApiUrlService } from '../../../../core/services/api-url.service';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  constructor(
    private http: HttpClient,
    private apiUrl: ApiUrlService
  ) {}

  getEvents(page: number = 0, size: number = 10): Observable<EventListResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<EventListResponse>(`${this.apiUrl.getUrl()}/api/admin/events`, { params });
  }

  getEvent(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl.getUrl()}/api/admin/events/${id}`);
  }

  createEvent(event: Partial<Event>): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl.getUrl()}/api/admin/events`, event);
  }

  updateEvent(id: number, event: Partial<Event>): Observable<Event> {
    return this.http.put<Event>(`${this.apiUrl.getUrl()}/api/admin/events/${id}`, event);
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl.getUrl()}/api/admin/events/${id}`);
  }
}