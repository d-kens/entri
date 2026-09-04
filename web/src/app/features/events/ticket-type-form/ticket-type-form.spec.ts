import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';

import { TicketTypeForm } from './ticket-type-form';

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
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
