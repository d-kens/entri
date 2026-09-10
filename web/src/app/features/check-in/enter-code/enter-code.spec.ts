import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { EnterCode } from './enter-code';
import { CheckInSessionService } from '../services/check-in-session-service';
import { CheckInService } from '../services/check-in-service';

describe('EnterCode', () => {
  let component: EnterCode;
  let fixture: ComponentFixture<EnterCode>;
  let checkIn: { verifyCode: ReturnType<typeof vi.fn> };
  let session: { setCode: ReturnType<typeof vi.fn>; setEventTitle: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    checkIn = { verifyCode: vi.fn() };
    session = { setCode: vi.fn(), setEventTitle: vi.fn() };
    router = { navigate: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [EnterCode],
      providers: [
        { provide: CheckInService, useValue: checkIn },
        { provide: CheckInSessionService, useValue: session },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EnterCode);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not call verifyCode when the code is blank', async () => {
    component.code = '   ';
    await component.submit();

    expect(checkIn.verifyCode).not.toHaveBeenCalled();
    expect(component.loading()).toBe(false);
  });

  it('should trim the code, store the session and navigate to the scanner on success', async () => {
    checkIn.verifyCode.mockResolvedValue({
      eventExternalId: 'evt-1',
      eventTitle: 'Music Night',
    });
    component.code = '  1234  ';

    await component.submit();

    expect(checkIn.verifyCode).toHaveBeenCalledWith('1234');
    expect(session.setCode).toHaveBeenCalledWith('1234');
    expect(session.setEventTitle).toHaveBeenCalledWith('Music Night');
    expect(router.navigate).toHaveBeenCalledWith(['/check-in/scanner']);
    expect(component.loading()).toBe(false);
    expect(component.error()).toBeNull();
  });

  it('should set the error message and not navigate when verifyCode fails with an Error', async () => {
    checkIn.verifyCode.mockRejectedValue(new Error('Invalid or expired code'));
    component.code = '9999';

    await component.submit();

    expect(component.error()).toBe('Invalid or expired code');
    expect(router.navigate).not.toHaveBeenCalled();
    expect(component.loading()).toBe(false);
  });

  it('should fall back to a generic error message when a non-Error is thrown', async () => {
    checkIn.verifyCode.mockRejectedValue('boom');
    component.code = '9999';

    await component.submit();

    expect(component.error()).toBe('Something went wrong. Try again.');
  });
});
