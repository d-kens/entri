import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EventDetailsForm } from './event-details-form';

describe('EventDetailsForm', () => {
  let component: EventDetailsForm;
  let fixture: ComponentFixture<EventDetailsForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventDetailsForm],
    }).compileComponents();

    fixture = TestBed.createComponent(EventDetailsForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
