import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Event } from '../../models/event.model';
import { EventService } from '../../services/event.service';
import { ConfirmModalComponent } from '../../../../../shared/components/confirm-modal/confirm-modal.component';

type SortField = 'name' | 'startDate' | 'endDate';
type SortDirection = 'asc' | 'desc';

@Component({
  selector: 'app-event-list',
  templateUrl: './event-list.component.html',
  styleUrls: ['./event-list.component.scss']
})
export class EventListComponent implements OnInit {
  events: Event[] = [];
  totalEvents = 0;
  page = 1;
  pageSize = 10;
  loading = false;
  filterActive = false;
  sortField: SortField = 'startDate';
  sortDirection: SortDirection = 'desc';

  constructor(
    private router: Router,
    private eventService: EventService,
    private modalService: NgbModal
  ) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading = true;
    // Convert from 1-based to 0-based page index for backend
    const pageIndex = this.page - 1;
    
    this.eventService.getEvents(pageIndex, this.pageSize)
      .subscribe({
        next: (response) => {
          this.events = response.content;
          this.totalEvents = response.total;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading events:', error);
          this.loading = false;
        }
      });
  }

  onCreate(): void {
    this.router.navigate(['/admin/events/new']);
  }

  onView(event: Event): void {
    this.router.navigate(['/admin/events', event.id]);
  }

  onEdit(event: Event): void {
    this.router.navigate(['/admin/events', event.id, 'edit']);
  }

  onDelete(event: Event): void {
    const modalRef = this.modalService.open(ConfirmModalComponent);
    modalRef.componentInstance.title = 'Delete Event';
    modalRef.componentInstance.message = `Are you sure you want to delete event "${event.name}"?`;
    modalRef.componentInstance.confirmText = 'Delete';
    modalRef.componentInstance.type = 'danger';

    modalRef.closed.subscribe(result => {
      if (result) {
        this.loading = true;
        this.eventService.deleteEvent(event.id)
          .subscribe({
            next: () => {
              this.loadEvents();
            },
            error: (error) => {
              console.error('Error deleting event:', error);
              this.loading = false;
            }
          });
      }
    });
  }

  onToggleStatus(event: Event): void {
    const action = event.active ? 'deactivate' : 'activate';
    const modalRef = this.modalService.open(ConfirmModalComponent);
    modalRef.componentInstance.title = `${action.charAt(0).toUpperCase() + action.slice(1)} Event`;
    modalRef.componentInstance.message = `Are you sure you want to ${action} event "${event.name}"?`;
    modalRef.componentInstance.confirmText = action.charAt(0).toUpperCase() + action.slice(1);
    modalRef.componentInstance.type = event.active ? 'warning' : 'primary';

    modalRef.closed.subscribe(result => {
      if (result) {
        this.loading = true;
        const updatedEvent = { ...event, active: !event.active };
        this.eventService.updateEvent(event.id, updatedEvent)
          .subscribe({
            next: () => {
              this.loadEvents();
            },
            error: (error) => {
              console.error('Error updating event:', error);
              this.loading = false;
            }
          });
      }
    });
  }

  onFilterChange(value: boolean): void {
    this.filterActive = value;
    this.page = 1;
    this.loadEvents();
  }

  onSort(field: SortField): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.loadEvents();
  }

  getSortIconClass(field: SortField): string {
    if (this.sortField !== field) {
      return 'bi-chevron-expand';
    }
    return this.sortDirection === 'asc' ? 'bi-chevron-up' : 'bi-chevron-down';
  }

  onPageChange(page: number): void {
    this.page = page;
    this.loadEvents();
  }

  getStartIndex(): number {
    return (this.page - 1) * this.pageSize + 1;
  }

  getEndIndex(): number {
    return Math.min(this.page * this.pageSize, this.totalEvents);
  }

  trackByEventId(index: number, event: Event): number {
    return event.id;
  }
}