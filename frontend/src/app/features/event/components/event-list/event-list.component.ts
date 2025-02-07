import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { EventService } from '../../services/event.service';
import { Event, EventStatus } from '../../models/event.model';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-event-list',
  templateUrl: './event-list.component.html',
  styleUrls: ['./event-list.component.scss']
})
export class EventListComponent implements OnInit {
  events: Event[] = [];
  totalEvents = 0;
  currentPage = 0;
  pageSize = 10;
  loading = false;
  EventStatus = EventStatus; // Make enum available in template

  constructor(
    private eventService: EventService,
    private router: Router,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading = true;
    this.eventService.getEvents(this.currentPage, this.pageSize)
      .subscribe({
        next: (response) => {
          this.events = response.content;
          this.totalEvents = response.totalElements;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  onPageChange(page: number): void {
    this.currentPage = page - 1; // NgbPagination uses 1-based indexing
    this.loadEvents();
  }

  viewEventDetails(event: Event): void {
    this.router.navigate(['/events', event.id]);
  }

  getStatusBadgeClass(status: EventStatus): string {
    switch (status) {
      case EventStatus.ACTIVE:
        return 'badge bg-success';
      case EventStatus.DRAFT:
        return 'badge bg-warning';
      case EventStatus.COMPLETED:
        return 'badge bg-info';
      case EventStatus.CANCELLED:
        return 'badge bg-danger';
      default:
        return 'badge bg-secondary';
    }
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  calculateProgress(event: Event): number {
    if (event.totalRewards === 0) return 0;
    return ((event.totalRewards - event.remainingRewards) / event.totalRewards) * 100;
  }
}