import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateTicketType } from './create-ticket-type';

describe('CreateTicketType', () => {
  let component: CreateTicketType;
  let fixture: ComponentFixture<CreateTicketType>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateTicketType],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateTicketType);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
