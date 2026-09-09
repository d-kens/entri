import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';

import { EventDetailsForm, EventDetailsFormData } from './event-details-form';

function validFormData(overrides: Partial<EventDetailsFormData> = {}): EventDetailsFormData {
  const today = new Date();
  const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000);
  return {
    title: 'My Event',
    description: 'A long enough description of the event.',
    categoryId: 1,
    currency: 'KES',
    bannerUrl: 'https://example.com/banner.png',
    bannerFile: null,
    venueName: 'Venue',
    venueCity: 'Nairobi',
    venueCountry: 'Kenya',
    startDate: today,
    startTime: '10:00',
    endDate: tomorrow,
    endTime: '12:00',
    ...overrides,
  };
}

function fillForm(component: EventDetailsForm, data: EventDetailsFormData): void {
  Object.entries(data).forEach(([key, value]) => {
    (component.eventDetailsForm as any)[key]().value.set(value);
  });
}

describe('EventDetailsForm', () => {
  let component: EventDetailsForm;
  let fixture: ComponentFixture<EventDetailsForm>;

  beforeEach(async () => {
    vi.stubGlobal('URL', {
      ...URL,
      createObjectURL: vi.fn(() => 'blob:mock-url'),
      revokeObjectURL: vi.fn(),
    });

    await TestBed.configureTestingModule({
      imports: [EventDetailsForm],
      providers: [provideNativeDateAdapter()],
    }).compileComponents();

    fixture = TestBed.createComponent(EventDetailsForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should default loading, submit label, and categories inputs', () => {
    expect(component.loading()).toBe(false);
    expect(component.formSubmitButtonLabel()).toBe('Create Event');
    expect(component.eventCategories()).toEqual([]);
  });

  it('should populate the form and banner preview when data input is set', async () => {
    const data = validFormData({ bannerUrl: 'https://example.com/existing.png' });
    fixture.componentRef.setInput('data', data);
    await fixture.whenStable();

    expect(component.eventDetailsForm.title().value()).toBe(data.title);
    expect(component.eventDetailsForm.venueCity().value()).toBe(data.venueCity);
    expect(component.bannerPreview()).toBe('https://example.com/existing.png');
  });

  it('should update bannerFile and preview when a banner is selected', () => {
    const file = new File(['content'], 'banner.png', { type: 'image/png' });
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { value: [file] });

    component.onBannerSelected({ target: input } as unknown as Event);

    expect(component.bannerPreview()).toBe('blob:mock-url');
    expect(component.eventDetailsForm.bannerFile().value()).toBe(file);
  });

  it('should do nothing when banner selection has no file', () => {
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { value: [] });

    component.onBannerSelected({ target: input } as unknown as Event);

    expect(component.bannerPreview()).toBeNull();
  });

  it('should clear banner preview, file, and url when removeBanner is called', () => {
    const file = new File(['content'], 'banner.png', { type: 'image/png' });
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { value: [file] });
    component.onBannerSelected({ target: input } as unknown as Event);

    component.removeBanner();

    expect(component.bannerPreview()).toBeNull();
    expect(component.eventDetailsForm.bannerFile().value()).toBeNull();
    expect(component.eventDetailsForm.bannerUrl().value()).toBe('');
  });

  it('should not emit submitted when the form is invalid', async () => {
    const submitted = vi.fn();
    component.submitted.subscribe(submitted);
    const event = new Event('submit');

    await component.onSubmit(event);

    expect(submitted).not.toHaveBeenCalled();
  });

  it('should emit submitted with the form data when the form is valid', async () => {
    const submitted = vi.fn();
    component.submitted.subscribe(submitted);
    fillForm(component, validFormData());
    const event = new Event('submit');
    const preventDefault = vi.spyOn(event, 'preventDefault');

    await component.onSubmit(event);

    expect(preventDefault).toHaveBeenCalled();
    expect(submitted).toHaveBeenCalledWith(expect.objectContaining({ title: 'My Event' }));
  });

  it('should reject a start date in the past when creating (no data input)', async () => {
    const submitted = vi.fn();
    component.submitted.subscribe(submitted);
    const past = new Date(Date.now() - 24 * 60 * 60 * 1000);
    fillForm(component, validFormData({ startDate: past }));

    await component.onSubmit(new Event('submit'));

    expect(submitted).not.toHaveBeenCalled();
    expect(component.eventDetailsForm.startDate().errors().length).toBeGreaterThan(0);
  });

  it('should reject an end date before the start date', async () => {
    const submitted = vi.fn();
    component.submitted.subscribe(submitted);
    const today = new Date();
    const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);
    fillForm(component, validFormData({ startDate: today, endDate: yesterday }));

    await component.onSubmit(new Event('submit'));

    expect(submitted).not.toHaveBeenCalled();
    expect(component.eventDetailsForm.endDate().errors().length).toBeGreaterThan(0);
  });

  it('should not apply the not-in-past validation when editing existing data', async () => {
    const past = new Date(Date.now() - 24 * 60 * 60 * 1000);
    const data = validFormData({ startDate: past, endDate: new Date() });
    fixture.componentRef.setInput('data', data);
    await fixture.whenStable();

    expect(component.eventDetailsForm.startDate().errors()).toEqual([]);
  });

  it('should require a banner url or file', async () => {
    const submitted = vi.fn();
    component.submitted.subscribe(submitted);
    fillForm(component, validFormData({ bannerUrl: '', bannerFile: null }));

    await component.onSubmit(new Event('submit'));

    expect(submitted).not.toHaveBeenCalled();
    expect(component.eventDetailsForm.bannerUrl().errors().length).toBeGreaterThan(0);
  });
});
