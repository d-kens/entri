import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';

import { TicketTypeForm, TicketTypeFormData } from './ticket-type-form';

describe('TicketTypeForm', () => {
  let component: TicketTypeForm;
  let fixture: ComponentFixture<TicketTypeForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketTypeForm],
      providers: [provideNativeDateAdapter()],
    }).compileComponents();

    fixture = TestBed.createComponent(TicketTypeForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  function setInputValue(placeholder: string, value: string) {
    const input: HTMLInputElement = fixture.nativeElement.querySelector(
      `input[placeholder="${placeholder}"]`,
    );
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function submitForm() {
    const form: HTMLFormElement = fixture.nativeElement.querySelector('form');
    form.dispatchEvent(new Event('submit', { cancelable: true }));
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not emit added when required fields are missing', async () => {
    const emitted = vi.fn();
    component.added.subscribe(emitted);

    submitForm();
    await fixture.whenStable();

    expect(emitted).not.toHaveBeenCalled();
  });

  it('should show validation errors after submitting an empty form', async () => {
    submitForm();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Name is required');
  });

  it('should reject a negative price', async () => {
    setInputValue('e.g. General Admission', 'VIP');
    setInputValue('0.00', '-5');
    setInputValue('100', '10');
    setInputValue('No limit', '2');

    submitForm();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Price cannot be negative');
  });

  it('should emit added with the entered data when the form is valid', async () => {
    const emitted = vi.fn();
    component.added.subscribe(emitted);

    setInputValue('e.g. General Admission', 'VIP');
    setInputValue('0.00', '50');
    setInputValue('100', '10');
    setInputValue('No limit', '2');

    submitForm();
    await fixture.whenStable();

    expect(emitted).toHaveBeenCalledWith(
      expect.objectContaining({
        name: 'VIP',
        price: '50',
        quantity: '10',
        maxTicketsPerOrder: '2',
      }),
    );
  });

  it('should populate the form fields when initialData changes', () => {
    const data: TicketTypeFormData = {
      name: 'Early Bird',
      price: 20,
      quantity: 5,
      description: 'desc',
      salesStartDate: '',
      salesEndDate: '',
      maxTicketsPerOrder: 1,
    };
    fixture.componentRef.setInput('initialData', data);
    fixture.detectChanges();

    const nameInput: HTMLInputElement = fixture.nativeElement.querySelector(
      'input[placeholder="e.g. General Admission"]',
    );
    expect(nameInput.value).toBe('Early Bird');
  });

  it('should compute min/max date from the event start/end time inputs', () => {
    fixture.componentRef.setInput('eventStartTime', '2026-06-01T10:00:00Z');
    fixture.componentRef.setInput('eventEndTime', '2026-06-02T10:00:00Z');
    fixture.detectChanges();

    expect(component.minDate()).toEqual(new Date('2026-06-01T10:00:00Z'));
    expect(component.maxDate()).toEqual(new Date('2026-06-02T10:00:00Z'));
  });

  it('should default minDate/maxDate to null when no event times are provided', () => {
    expect(component.minDate()).toBeNull();
    expect(component.maxDate()).toBeNull();
  });
});
