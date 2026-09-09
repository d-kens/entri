import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PLATFORM_ID, signal } from '@angular/core';
import { Router } from '@angular/router';

import { Scanner } from './scanner';
import { CheckInService, CheckInResponse } from '../services/check-in-service';
import { CheckInSessionService } from '../services/check-in-session-service';

describe('Scanner', () => {
  let component: Scanner;
  let fixture: ComponentFixture<Scanner>;
  let checkIn: { checkIn: ReturnType<typeof vi.fn> };
  let session: {
    eventTitle: ReturnType<typeof signal<string | null>>;
    checkInCode: ReturnType<typeof signal<string | null>>;
    clearCode: ReturnType<typeof vi.fn>;
  };
  let router: { navigate: ReturnType<typeof vi.fn> };
  let getUserMedia: ReturnType<typeof vi.fn>;

  function setup(platformId: 'browser' | 'server' = 'browser') {
    return TestBed.configureTestingModule({
      imports: [Scanner],
      providers: [
        { provide: PLATFORM_ID, useValue: platformId },
        { provide: CheckInService, useValue: checkIn },
        { provide: CheckInSessionService, useValue: session },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();
  }

  beforeEach(() => {
    checkIn = { checkIn: vi.fn() };
    session = {
      eventTitle: signal<string | null>('Music Night'),
      checkInCode: signal<string | null>('CODE1'),
      clearCode: vi.fn(),
    };
    router = { navigate: vi.fn() };

    getUserMedia = vi.fn().mockRejectedValue(new Error('camera denied'));
    Object.defineProperty(navigator, 'mediaDevices', {
      value: { getUserMedia },
      configurable: true,
    });
  });

  it('should create and mark a camera error when getUserMedia rejects', async () => {
    await setup();
    fixture = TestBed.createComponent(Scanner);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component).toBeTruthy();
    expect(getUserMedia).toHaveBeenCalled();
    expect(component.cameraError).toBe(true);
  });

  it('should not request the camera when rendered on the server', async () => {
    await setup('server');
    fixture = TestBed.createComponent(Scanner);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(getUserMedia).not.toHaveBeenCalled();
  });

  describe('manual code submission', () => {
    beforeEach(async () => {
      await setup();
      fixture = TestBed.createComponent(Scanner);
      component = fixture.componentInstance;
      fixture.detectChanges();
      await fixture.whenStable();
    });

    it('should not call checkIn when the manual code is blank', async () => {
      component.manualCode = '   ';
      await component.submitManual();

      expect(checkIn.checkIn).not.toHaveBeenCalled();
    });

    it('should trim the manual code and mark the result valid', async () => {
      const response: CheckInResponse = {
        result: 'VALID',
        holderName: 'Jane Doe',
        ticketType: 'VIP',
        checkedInAt: null,
      };
      checkIn.checkIn.mockResolvedValue(response);
      component.manualCode = '  TICKET1  ';

      await component.submitManual();

      expect(checkIn.checkIn).toHaveBeenCalledWith('TICKET1', 'CODE1');
      expect(component.resultState()).toBe('valid');
      expect(component.checkInResponse()).toEqual(response);
      expect(component.manualCode).toBe('');
    });

    it('should mark the result as used when the ticket was already checked in', async () => {
      checkIn.checkIn.mockResolvedValue({
        result: 'ALREADY_USED',
        holderName: 'Jane Doe',
        ticketType: 'VIP',
        checkedInAt: '2026-01-01T10:00:00Z',
      });
      component.manualCode = 'TICKET1';

      await component.submitManual();

      expect(component.resultState()).toBe('used');
    });

    it('should mark the result invalid for any other outcome', async () => {
      checkIn.checkIn.mockResolvedValue({
        result: 'INVALID',
        holderName: '',
        ticketType: '',
        checkedInAt: null,
      });
      component.manualCode = 'TICKET1';

      await component.submitManual();

      expect(component.resultState()).toBe('invalid');
    });

    it('should set an error state when checkIn fails', async () => {
      checkIn.checkIn.mockRejectedValue(new Error('Something went wrong. Try again.'));
      component.manualCode = 'TICKET1';

      await component.submitManual();

      expect(component.resultState()).toBe('error');
      expect(component.errorMsg()).toBe('Something went wrong. Try again.');
    });
  });

  describe('reset and logout', () => {
    beforeEach(async () => {
      await setup();
      fixture = TestBed.createComponent(Scanner);
      component = fixture.componentInstance;
      fixture.detectChanges();
      await fixture.whenStable();
    });

    it('should clear the result state on reset', () => {
      component.resultState.set('valid');
      component.checkInResponse.set({
        result: 'VALID',
        holderName: 'Jane',
        ticketType: 'VIP',
        checkedInAt: null,
      });
      component.errorMsg.set('oops');

      component.reset();

      expect(component.resultState()).toBe('none');
      expect(component.checkInResponse()).toBeNull();
      expect(component.errorMsg()).toBeNull();
    });

    it('should clear the session and navigate back to check-in on logout', () => {
      component.logout();

      expect(session.clearCode).toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['/check-in']);
    });
  });
});
