import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditTicketType } from './edit-ticket-type';

describe('EditTicketType', () => {
  let component: EditTicketType;
  let fixture: ComponentFixture<EditTicketType>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditTicketType],
    }).compileComponents();

    fixture = TestBed.createComponent(EditTicketType);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
