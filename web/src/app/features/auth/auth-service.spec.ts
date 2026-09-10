import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PLATFORM_ID } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth-service';
import { AuthResponse, AccessTokenResponse } from './models/auth.models';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should return null token when nothing is stored', () => {
    expect(service.getToken()).toBeNull();
  });

  it('should report not authenticated when there is no token', () => {
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should report authenticated once a token is stored', () => {
    localStorage.setItem('access_token', 'some-token');
    expect(service.isAuthenticated()).toBe(true);
  });

  it('should return null role/externalId when token is malformed', () => {
    localStorage.setItem('access_token', 'not-a-jwt');
    expect(service.getRole()).toBeNull();
    expect(service.getExternalId()).toBeNull();
  });

  it('should decode role and externalId from a valid jwt-shaped token', () => {
    const payload = btoa(JSON.stringify({ role: 'ORGANIZER', sub: 'ext-123' }));
    localStorage.setItem('access_token', `header.${payload}.signature`);
    expect(service.getRole()).toBe('ORGANIZER');
    expect(service.getExternalId()).toBe('ext-123');
  });

  it('should POST to register and return the created user', () => {
    const request = {
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      phoneNumber: '0712345678',
      password: 'Password123!',
      role: 'ORGANIZER',
    };
    let response: unknown;
    service.register(request).subscribe((res) => (response = res));

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    const user = { externalKey: 'ext-1', email: request.email } as never;
    req.flush(user);
    expect(response).toEqual(user);
  });

  it('should store the access token and mark authenticated on successful login', () => {
    const authResponse: AuthResponse = {
      user: {
        role: 'ORGANIZER',
        email: 'john@example.com',
        externalKey: 'ext-1',
        firstName: 'John',
        lastName: 'Doe',
        phoneNumber: '0712345678',
        enabled: true,
      },
      accessToken: { token: 'access-token', expiresIn: 900 },
    };

    service.login({ email: 'john@example.com', password: 'secret' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    req.flush(authResponse);

    expect(localStorage.getItem('access_token')).toBe('access-token');
    expect(service.isAuthenticated()).toBe(true);
  });

  it('should propagate the error and not authenticate on failed login', () => {
    let errored = false;
    service.login({ email: 'john@example.com', password: 'wrong' }).subscribe({
      error: () => (errored = true),
    });

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/login`);
    req.flush({ detail: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });

    expect(errored).toBe(true);
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should store the new access token on refreshToken success', () => {
    const response: AccessTokenResponse = { token: 'new-token', expiresIn: 900 };
    service.refreshToken().subscribe();

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/refresh-token`);
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    req.flush(response);

    expect(localStorage.getItem('access_token')).toBe('new-token');
    expect(service.isAuthenticated()).toBe(true);
  });

  it('should clear the stored token on clearSession', () => {
    localStorage.setItem('access_token', 'some-token');
    service.clearSession();
    expect(localStorage.getItem('access_token')).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should not touch localStorage on clearSession when not running in a browser', () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: PLATFORM_ID, useValue: 'server' },
      ],
    });
    localStorage.setItem('access_token', 'some-token');
    const serverService = TestBed.inject(AuthService);

    serverService.clearSession();

    expect(localStorage.getItem('access_token')).toBe('some-token');
    expect(serverService.getToken()).toBeNull();
    TestBed.inject(HttpTestingController).verify();
  });

  it('should remove the token on logout even when the request errors', () => {
    localStorage.setItem('access_token', 'some-token');
    let errored = false;
    service.logout().subscribe({ error: () => (errored = true) });

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/logout`);
    expect(req.request.withCredentials).toBe(true);
    req.flush({}, { status: 500, statusText: 'Server Error' });

    expect(errored).toBe(true);
    expect(localStorage.getItem('access_token')).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should POST the email for forgotPassword', () => {
    service.forgotPassword('john@example.com').subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/forgot-password`);
    expect(req.request.body).toEqual({ email: 'john@example.com' });
    req.flush(null);
  });

  it('should POST the reset request for resetPassword', () => {
    const request = { token: 'reset-token', password: 'NewPassword123!' };
    service.resetPassword(request).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/reset-password`);
    expect(req.request.body).toEqual(request);
    req.flush(null);
  });
});
