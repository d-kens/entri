import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Public } from './public';

describe('Public', () => {
  let component: Public;
  let fixture: ComponentFixture<Public>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Public],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Public);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the nav bar', () => {
    expect(fixture.nativeElement.querySelector('app-nav-bar')).toBeTruthy();
  });
});
