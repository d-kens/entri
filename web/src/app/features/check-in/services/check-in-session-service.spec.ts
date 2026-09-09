import { TestBed } from '@angular/core/testing';
import { PLATFORM_ID } from '@angular/core';
import { CheckInSessionService } from './check-in-session-service';

describe('CheckInSessionService', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  function createService(): CheckInSessionService {
    TestBed.configureTestingModule({ providers: [CheckInSessionService] });
    return TestBed.inject(CheckInSessionService);
  }

  it('should initialize signals from empty sessionStorage as null', () => {
    const service = createService();
    expect(service.checkInCode()).toBeNull();
    expect(service.eventTitle()).toBeNull();
  });

  it('should initialize signals from previously stored sessionStorage values', () => {
    sessionStorage.setItem('checkInCode', 'ABC123');
    sessionStorage.setItem('eventTitle', 'Jazz Night');
    const service = createService();
    expect(service.checkInCode()).toBe('ABC123');
    expect(service.eventTitle()).toBe('Jazz Night');
  });

  it('should persist and expose the check-in code on setCode', () => {
    const service = createService();
    service.setCode('XYZ789');
    expect(service.checkInCode()).toBe('XYZ789');
    expect(sessionStorage.getItem('checkInCode')).toBe('XYZ789');
  });

  it('should persist and expose the event title on setEventTitle', () => {
    const service = createService();
    service.setEventTitle('Rock Fest');
    expect(service.eventTitle()).toBe('Rock Fest');
    expect(sessionStorage.getItem('eventTitle')).toBe('Rock Fest');
  });

  it('should clear both the code and title from state and storage on clearCode', () => {
    const service = createService();
    service.setCode('XYZ789');
    service.setEventTitle('Rock Fest');

    service.clearCode();

    expect(service.checkInCode()).toBeNull();
    expect(service.eventTitle()).toBeNull();
    expect(sessionStorage.getItem('checkInCode')).toBeNull();
    expect(sessionStorage.getItem('eventTitle')).toBeNull();
  });

  it('should not touch sessionStorage when not running in a browser', () => {
    sessionStorage.setItem('checkInCode', 'ABC123');
    TestBed.configureTestingModule({
      providers: [CheckInSessionService, { provide: PLATFORM_ID, useValue: 'server' }],
    });
    const service = TestBed.inject(CheckInSessionService);

    expect(service.checkInCode()).toBeNull();

    service.setCode('SHOULD-NOT-PERSIST');
    expect(sessionStorage.getItem('checkInCode')).toBe('ABC123');
  });
});
