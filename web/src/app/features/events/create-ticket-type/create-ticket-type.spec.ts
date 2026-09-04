import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { CreateTicketType } from './create-ticket-type';

describe('CreateTicketType', () => {
  let component: CreateTicketType;
  let fixture: ComponentFixture<CreateTicketType>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateTicketType],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateTicketType);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
