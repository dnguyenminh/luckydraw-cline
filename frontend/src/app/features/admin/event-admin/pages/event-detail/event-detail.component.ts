import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { finalize } from 'rxjs/operators';
import { Event, Reward } from '../../models/event.model';
import { EventService } from '../../services/event.service';
import { ConfirmModalComponent } from '../../../../../shared/components/confirm-modal/confirm-modal.component';

@Component({
  selector: 'app-event-detail',
  templateUrl: './event-detail.component.html',
  styleUrls: ['./event-detail.component.scss']
})
export class EventDetailComponent implements OnInit {
  event: Event | null = null;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService,
    private modalService: NgbModal
  ) {}

  ngOnInit(): void {
    const eventId = this.route.snapshot.params['id'];
    if (eventId) {
      this.loadEvent(+eventId);
    }
  }

  loadEvent(id: number): void {
    this.loading = true;
    this.eventService.getEvent(id)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (event) => this.event = event,
        error: (error) => {
          console.error('Error loading event:', error);
          this.router.navigate(['/admin/events']);
        }
      });
  }

  getActiveRewardsCount(): number {
    if (!this.event?.rewards) return 0;
    return this.event.rewards.filter(reward => reward.active).length;
  }

  addReward(): void {
    // To be implemented - open reward form modal
    console.log('Add reward clicked');
  }

  editReward(reward: Reward): void {
    // To be implemented - open reward form modal with existing reward
    console.log('Edit reward clicked:', reward);
  }

  deleteReward(reward: Reward): void {
    const modalRef = this.modalService.open(ConfirmModalComponent);
    modalRef.componentInstance.title = 'Delete Reward';
    modalRef.componentInstance.message = `Are you sure you want to delete reward "${reward.name}"?`;
    modalRef.componentInstance.confirmText = 'Delete';
    modalRef.componentInstance.type = 'danger';

    modalRef.closed.subscribe(result => {
      if (result) {
        // To be implemented - delete reward API call
        console.log('Delete reward confirmed:', reward);
      }
    });
  }
}