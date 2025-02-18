import { Component, OnInit } from '@angular/core';
import { Event, EventService } from '../../services/event.service';
import { LoadingService } from '@core/services/loading.service';
import { AlertService } from '@core/services/alert.service';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-event-list',
  templateUrl: './event-list.component.html',
  styleUrls: ['./event-list.component.scss']
})
export class EventListComponent implements OnInit {
  events: Event[] = [];
  loading = false;
  filterActive = false;
  totalEvents = 0;
  page = 1;
  pageSize = 10;

  constructor(
    private eventService: EventService,
    private loadingService: LoadingService,
    private alertService: AlertService
  ) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading = true;
    const params = {
      page: this.page - 1,
      size: this.pageSize,
      active: this.filterActive
    };

    this.eventService.getEvents(params)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (events) => {
          this.events = events;
          this.totalEvents = events.length;
        },
        error: () => {
          this.alertService.error('Failed to load events');
        }
      });
  }

  onFilterChange(value: boolean): void {
    this.filterActive = value;
    this.loadEvents();
  }

  onPageChange(page: number): void {
    this.page = page;
    this.loadEvents();
  }

  onCreate(): void {
    // Will be implemented later
  }
}