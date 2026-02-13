import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeliveryPayment } from './delivery-payment';

describe('DeliveryPayment', () => {
  let component: DeliveryPayment;
  let fixture: ComponentFixture<DeliveryPayment>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeliveryPayment]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DeliveryPayment);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
