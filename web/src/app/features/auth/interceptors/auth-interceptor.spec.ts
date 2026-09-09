import { TestBed } from '@angular/core/testing';
import {
  HttpErrorResponse,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';
import { lastValueFrom, of, throwError } from 'rxjs';

import { authInterceptor } from './auth-interceptor';
import { AuthService } from '../auth-service';
import { SnackbarService } from '@shared/services/snackbar-service';

describe('authInterceptor', () => {
  let authServiceMock: {
    getToken: ReturnType<typeof vi.fn>;
    refreshToken: ReturnType<typeof vi.fn>;
    clearSession: ReturnType<typeof vi.fn>;
  };
  let snackbarMock: { showError: ReturnType<typeof vi.fn> };
  let router: Router;

  const interceptor: HttpInterceptorFn = (req, next) =>
    TestBed.runInInjectionContext(() => authInterceptor(req, next));

  beforeEach(() => {
    authServiceMock = {
      getToken: vi.fn(),
      refreshToken: vi.fn(),
      clearSession: vi.fn(),
    };
    snackbarMock = { showError: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock },
        { provide: SnackbarService, useValue: snackbarMock },
      ],
    });
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  it('attaches the bearer token when one is present', async () => {
    authServiceMock.getToken.mockReturnValue('access-token');
    const req = new HttpRequest('GET', '/api/events');
    const next: HttpHandlerFn = vi.fn(() => of(new HttpResponse({ status: 200 })));

    await lastValueFrom(interceptor(req, next));

    const forwardedReq = (next as any).mock.calls[0][0] as HttpRequest<unknown>;
    expect(forwardedReq.headers.get('Authorization')).toBe('Bearer access-token');
  });

  it('does not attach a token when none is present', async () => {
    authServiceMock.getToken.mockReturnValue(null);
    const req = new HttpRequest('GET', '/api/events');
    const next: HttpHandlerFn = vi.fn(() => of(new HttpResponse({ status: 200 })));

    await lastValueFrom(interceptor(req, next));

    const forwardedReq = (next as any).mock.calls[0][0] as HttpRequest<unknown>;
    expect(forwardedReq.headers.has('Authorization')).toBe(false);
  });

  it('does not attach a token on the refresh-token call itself', async () => {
    authServiceMock.getToken.mockReturnValue('access-token');
    const req = new HttpRequest('POST', '/api/auth/refresh-token', {});
    const next: HttpHandlerFn = vi.fn(() => of(new HttpResponse({ status: 200 })));

    await lastValueFrom(interceptor(req, next));

    const forwardedReq = (next as any).mock.calls[0][0] as HttpRequest<unknown>;
    expect(forwardedReq.headers.has('Authorization')).toBe(false);
  });

  it('passes through non-401 errors without attempting a refresh', async () => {
    authServiceMock.getToken.mockReturnValue('access-token');
    const req = new HttpRequest('GET', '/api/events');
    const serverError = new HttpErrorResponse({ status: 500 });
    const next: HttpHandlerFn = vi.fn(() => throwError(() => serverError));

    await expect(lastValueFrom(interceptor(req, next))).rejects.toBe(serverError);
    expect(authServiceMock.refreshToken).not.toHaveBeenCalled();
  });

  it('does not attempt a refresh on 401 when the original request had no token', async () => {
    authServiceMock.getToken.mockReturnValue(null);
    const req = new HttpRequest('GET', '/api/events');
    const unauthorizedError = new HttpErrorResponse({ status: 401 });
    const next: HttpHandlerFn = vi.fn(() => throwError(() => unauthorizedError));

    await expect(lastValueFrom(interceptor(req, next))).rejects.toBe(unauthorizedError);
    expect(authServiceMock.refreshToken).not.toHaveBeenCalled();
  });

  it('refreshes the token and retries the request on a 401', async () => {
    authServiceMock.getToken.mockReturnValue('expired-token');
    authServiceMock.refreshToken.mockReturnValue(of({ token: 'new-token', expiresIn: 900 }));
    const req = new HttpRequest('GET', '/api/events');
    const unauthorizedError = new HttpErrorResponse({ status: 401 });
    const finalResponse = new HttpResponse({ status: 200 });
    const next: HttpHandlerFn = vi
      .fn()
      .mockReturnValueOnce(throwError(() => unauthorizedError))
      .mockReturnValueOnce(of(finalResponse));

    const result = await lastValueFrom(interceptor(req, next));

    expect(result).toBe(finalResponse);
    expect(next).toHaveBeenCalledTimes(2);
    const retriedReq = (next as any).mock.calls[1][0] as HttpRequest<unknown>;
    expect(retriedReq.headers.get('Authorization')).toBe('Bearer new-token');
  });

  it('clears the session and redirects to login when the refresh itself fails with 401', async () => {
    authServiceMock.getToken.mockReturnValue('expired-token');
    const refreshError = new HttpErrorResponse({ status: 401 });
    authServiceMock.refreshToken.mockReturnValue(throwError(() => refreshError));
    const req = new HttpRequest('GET', '/api/events');
    const unauthorizedError = new HttpErrorResponse({ status: 401 });
    const next: HttpHandlerFn = vi.fn(() => throwError(() => unauthorizedError));

    await expect(lastValueFrom(interceptor(req, next))).rejects.toBe(refreshError);

    expect(authServiceMock.clearSession).toHaveBeenCalled();
    expect(snackbarMock.showError).toHaveBeenCalledWith(
      'Your session has expired. Please log in again.',
    );
    expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
  });

  it('clears the session and redirects to login when the refresh fails with 403', async () => {
    authServiceMock.getToken.mockReturnValue('expired-token');
    const refreshError = new HttpErrorResponse({ status: 403 });
    authServiceMock.refreshToken.mockReturnValue(throwError(() => refreshError));
    const req = new HttpRequest('GET', '/api/events');
    const unauthorizedError = new HttpErrorResponse({ status: 401 });
    const next: HttpHandlerFn = vi.fn(() => throwError(() => unauthorizedError));

    await expect(lastValueFrom(interceptor(req, next))).rejects.toBe(refreshError);

    expect(authServiceMock.clearSession).toHaveBeenCalled();
  });

  it('does not clear the session when the refresh fails with a non-auth error', async () => {
    authServiceMock.getToken.mockReturnValue('expired-token');
    const refreshError = new HttpErrorResponse({ status: 500 });
    authServiceMock.refreshToken.mockReturnValue(throwError(() => refreshError));
    const req = new HttpRequest('GET', '/api/events');
    const unauthorizedError = new HttpErrorResponse({ status: 401 });
    const next: HttpHandlerFn = vi.fn(() => throwError(() => unauthorizedError));

    await expect(lastValueFrom(interceptor(req, next))).rejects.toBe(refreshError);

    expect(authServiceMock.clearSession).not.toHaveBeenCalled();
    expect(snackbarMock.showError).not.toHaveBeenCalled();
  });
});
