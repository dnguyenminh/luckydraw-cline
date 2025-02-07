import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiUrlService } from '../../../core/services/api-url.service';
import { 
  Event, 
  CreateEventRequest, 
  UpdateEventRequest, 
  EventSummary,
  EventParticipant,
  DrawResult 
} from '../models/event.model';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  constructor(
    private http: HttpClient,
    private apiUrl: ApiUrlService
  ) {}

  // Event CRUD operations
  getEvents(page: number = 0, size: number = 10): Observable<{ content: Event[], totalElements: number }> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<{ content: Event[], totalElements: number }>(
      this.apiUrl.getEventsUrl(),
      { params }
    );
  }

  getEvent(id: number): Observable<Event> {
    return this.http.get<Event>(this.apiUrl.getEventUrl(id));
  }

  createEvent(event: CreateEventRequest): Observable<Event> {
    return this.http.post<Event>(this.apiUrl.getEventsUrl(), event);
  }

  updateEvent(id: number, event: UpdateEventRequest): Observable<Event> {
    return this.http.patch<Event>(this.apiUrl.getEventUrl(id), event);
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(this.apiUrl.getEventUrl(id));
  }

  // Event summary and statistics
  getEventSummary(id: number): Observable<EventSummary> {
    return this.http.get<EventSummary>(`${this.apiUrl.getEventUrl(id)}/summary`);
  }

  // Participant management
  getEventParticipants(
    eventId: number,
    page: number = 0,
    size: number = 10,
    isDrawn?: boolean
  ): Observable<{ content: EventParticipant[], totalElements: number }> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (isDrawn !== undefined) {
      params = params.set('isDrawn', isDrawn.toString());
    }

    return this.http.get<{ content: EventParticipant[], totalElements: number }>(
      `${this.apiUrl.getEventUrl(eventId)}/participants`,
      { params }
    );
  }

  // Draw operations
  drawWinner(eventId: number): Observable<DrawResult> {
    return this.http.post<DrawResult>(`${this.apiUrl.getEventUrl(eventId)}/draw`, {});
  }

  getDrawHistory(
    eventId: number,
    page: number = 0,
    size: number = 10
  ): Observable<{ content: DrawResult[], totalElements: number }> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<{ content: DrawResult[], totalElements: number }>(
      `${this.apiUrl.getEventUrl(eventId)}/draw-history`,
      { params }
    );
  }

  // Event status management
  startEvent(id: number): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl.getEventUrl(id)}/start`, {});
  }

  completeEvent(id: number): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl.getEventUrl(id)}/complete`, {});
  }

  cancelEvent(id: number): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl.getEventUrl(id)}/cancel`, {});
  }
}