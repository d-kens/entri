import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { EventHero } from './event-hero';
import { EventResponse } from '@features/events/models/event.models';

const event: EventResponse = {
  externalId: 'evt-1',
  title: 'Sample Event',
  description: 'desc',
  categoryName: 'Music',
  categoryId: 1,
  currency: 'USD',
  venueName: 'Venue',
  venueCity: 'City',
  venueCountry: 'Country',
  startTime: '2026-01-01T10:00:00Z',
  endTime: '2026-01-01T12:00:00Z',
  bannerUrl: '',
  status: 'PUBLISHED',
  dateCreated: '2025-12-01T00:00:00Z',
};

describe('EventHero', () => {
  let component: EventHero;
  let fixture: ComponentFixture<EventHero>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventHero],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(EventHero);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('event', event);
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should require the event input', () => {
    expect(component.event()).toEqual(event);
  });

  it('should default backLink to the events dashboard', () => {
    expect(component.backLink()).toEqual(['/dashboard/events']);
  });

  it('should use a custom backLink when provided', () => {
    fixture.componentRef.setInput('backLink', ['/custom']);
    expect(component.backLink()).toEqual(['/custom']);
  });
});
