import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddEditTicketType } from './add-edit-ticket-type';

describe('AddEditTicketType', () => {
  let component: AddEditTicketType;
  let fixture: ComponentFixture<AddEditTicketType>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddEditTicketType],
    }).compileComponents();

    fixture = TestBed.createComponent(AddEditTicketType);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
