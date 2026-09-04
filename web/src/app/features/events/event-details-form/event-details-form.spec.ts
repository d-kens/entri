import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';

import { EventDetailsForm } from './event-details-form';

describe('EventDetailsForm', () => {
  let component: EventDetailsForm;
  let fixture: ComponentFixture<EventDetailsForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventDetailsForm],
      providers: [provideNativeDateAdapter()],
    }).compileComponents();

    fixture = TestBed.createComponent(EventDetailsForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
