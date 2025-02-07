import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EventGoldenHoursFormComponent } from './event-golden-hours-form.component';

describe('EventGoldenHoursFormComponent', () => {
  let component: EventGoldenHoursFormComponent;
  let fixture: ComponentFixture<EventGoldenHoursFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EventGoldenHoursFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EventGoldenHoursFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
