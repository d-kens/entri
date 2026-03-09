import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrackDelivery } from './track-delivery';

describe('TrackDelivery', () => {
  let component: TrackDelivery;
  let fixture: ComponentFixture<TrackDelivery>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrackDelivery]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrackDelivery);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
