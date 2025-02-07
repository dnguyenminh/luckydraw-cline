import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { Event } from '../../models/event.model';
import { EventService } from '../../services/event.service';

@Component({
  selector: 'app-event-form',
  templateUrl: './event-form.component.html',
  styleUrls: ['./event-form.component.scss']
})
export class EventFormComponent implements OnInit {
  eventForm: FormGroup;
  isEditMode = false;
  loading = false;

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService
  ) {
    this.eventForm = this.createForm();
  }

  ngOnInit(): void {
    const eventId = this.route.snapshot.params['id'];
    if (eventId) {
      this.isEditMode = true;
      this.loadEvent(+eventId);
    }
  }

  createForm(): FormGroup {
    return this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(500)]],
      startDate: ['', [Validators.required]],
      endDate: ['', [Validators.required]],
      defaultSpins: [0, [Validators.required, Validators.min(0)]],
      maxSpinsPerDay: [1, [Validators.required, Validators.min(1)]],
      active: [true]
    });
  }

  loadEvent(id: number): void {
    this.loading = true;
    this.eventService.getEvent(id)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (event) => this.patchForm(event),
        error: (error) => {
          console.error('Error loading event:', error);
          this.router.navigate(['/admin/events']);
        }
      });
  }

  patchForm(event: Event): void {
    this.eventForm.patchValue({
      name: event.name,
      description: event.description,
      startDate: this.formatDate(event.startDate),
      endDate: this.formatDate(event.endDate),
      defaultSpins: event.defaultSpins,
      maxSpinsPerDay: event.maxSpinsPerDay,
      active: event.active
    });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toISOString().slice(0, 16); // Format as YYYY-MM-DDThh:mm
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.eventForm.get(fieldName);
    return field ? (field.invalid && (field.dirty || field.touched)) : false;
  }

  onSubmit(): void {
    if (this.eventForm.invalid || this.loading) {
      return;
    }

    this.loading = true;
    const eventData = this.eventForm.value;

    const request$ = this.isEditMode
      ? this.eventService.updateEvent(this.route.snapshot.params['id'], eventData)
      : this.eventService.createEvent(eventData);

    request$
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: () => this.router.navigate(['/admin/events']),
        error: (error) => console.error('Error saving event:', error)
      });
  }

  onCancel(): void {
    this.router.navigate(['/admin/events']);
  }
}