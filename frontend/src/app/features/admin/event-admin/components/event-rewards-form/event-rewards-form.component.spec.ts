import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EventRewardsFormComponent } from './event-rewards-form.component';

describe('EventRewardsFormComponent', () => {
  let component: EventRewardsFormComponent;
  let fixture: ComponentFixture<EventRewardsFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EventRewardsFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EventRewardsFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
