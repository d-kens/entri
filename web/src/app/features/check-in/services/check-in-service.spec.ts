import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../../environments/environment';
import { CheckInService, CheckInResponse, VerifyCodeResponse } from './check-in-service';

describe('CheckInService', () => {
  let service: CheckInService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CheckInService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CheckInService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should POST the code and resolve with the verification response', async () => {
    const response: VerifyCodeResponse = { eventExternalId: 'ext-1', eventTitle: 'Jazz Night' };
    const promise = service.verifyCode('ABC123');

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/check-in/verify-code`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ code: 'ABC123' });
    req.flush(response);

    await expect(promise).resolves.toEqual(response);
  });

  it('should reject verifyCode with the server detail message on error', async () => {
    const promise = service.verifyCode('BAD');
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/check-in/verify-code`);
    req.flush({ detail: 'Code expired' }, { status: 400, statusText: 'Bad Request' });

    await expect(promise).rejects.toThrow('Code expired');
  });

  it('should reject verifyCode with the fallback message when there is no detail', async () => {
    const promise = service.verifyCode('BAD');
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/check-in/verify-code`);
    req.flush({}, { status: 500, statusText: 'Server Error' });

    await expect(promise).rejects.toThrow('Invalid or expired code');
  });

  it('should POST the ticket and check-in codes and resolve with the result', async () => {
    const response: CheckInResponse = {
      result: 'VALID',
      holderName: 'John Doe',
      ticketType: 'VIP',
      checkedInAt: '2026-01-01T10:00:00Z',
    };
    const promise = service.checkIn('ticket-1', 'ABC123');

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/tickets/ticket-1/check-in`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ checkInCode: 'ABC123' });
    req.flush(response);

    await expect(promise).resolves.toEqual(response);
  });

  it('should reject checkIn with the fallback message on error without detail', async () => {
    const promise = service.checkIn('ticket-1', 'ABC123');
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/tickets/ticket-1/check-in`);
    req.flush({}, { status: 500, statusText: 'Server Error' });

    await expect(promise).rejects.toThrow('Something went wrong. Try again.');
  });
});
