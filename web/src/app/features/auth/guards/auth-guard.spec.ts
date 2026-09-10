import { TestBed } from '@angular/core/testing';
import { CanActivateFn, provideRouter, Router, UrlTree } from '@angular/router';

import { authGuard } from './auth-guard';
import { AuthService } from '../auth-service';

describe('authGuard', () => {
  let authServiceMock: { isAuthenticated: ReturnType<typeof vi.fn> };
  let router: Router;

  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => authGuard(...guardParameters));

  beforeEach(() => {
    authServiceMock = { isAuthenticated: vi.fn() };
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: authServiceMock }],
    });
    router = TestBed.inject(Router);
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });

  it('allows activation when the user is authenticated', () => {
    authServiceMock.isAuthenticated.mockReturnValue(true);

    const result = executeGuard({} as any, { url: '/dashboard/events' } as any);

    expect(result).toBe(true);
  });

  it('redirects to /auth with the return url when the user is not authenticated', () => {
    authServiceMock.isAuthenticated.mockReturnValue(false);

    const result = executeGuard({} as any, { url: '/dashboard/events' } as any) as UrlTree;

    expect(router.serializeUrl(result)).toBe('/auth?returnUrl=%2Fdashboard%2Fevents');
  });
});
