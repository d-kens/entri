import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TicketTypeForm } from './ticket-type-form';

describe('TicketTypeForm', () => {
  let component: TicketTypeForm;
  let fixture: ComponentFixture<TicketTypeForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketTypeForm],
    }).compileComponents();

    fixture = TestBed.createComponent(TicketTypeForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
