import { TestBed } from '@angular/core/testing';
import { CanActivateFn, provideRouter, Router, UrlTree } from '@angular/router';

import { roleGuard } from './role-guard';
import { AuthService } from '@features/auth/auth-service';

describe('roleGuard', () => {
  let authServiceMock: { getRole: ReturnType<typeof vi.fn> };
  let router: Router;

  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => roleGuard(...guardParameters));

  beforeEach(() => {
    authServiceMock = { getRole: vi.fn() };
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: authServiceMock }],
    });
    router = TestBed.inject(Router);
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });

  it('allows activation when the user role is in the allowed roles list', () => {
    authServiceMock.getRole.mockReturnValue('ADMIN');

    const result = executeGuard({ data: { roles: ['ADMIN', 'ORGANIZER'] } } as any, {} as any);

    expect(result).toBe(true);
  });

  it('redirects to /dashboard/events when the user role is not in the allowed roles list', () => {
    authServiceMock.getRole.mockReturnValue('ATTENDEE');

    const result = executeGuard(
      { data: { roles: ['ADMIN', 'ORGANIZER'] } } as any,
      {} as any,
    ) as UrlTree;

    expect(router.serializeUrl(result)).toBe('/dashboard/events');
  });

  it('redirects to /dashboard/events when no roles are configured on the route', () => {
    authServiceMock.getRole.mockReturnValue('ADMIN');

    const result = executeGuard({ data: {} } as any, {} as any) as UrlTree;

    expect(router.serializeUrl(result)).toBe('/dashboard/events');
  });

  it('redirects to /dashboard/events when the user has no role', () => {
    authServiceMock.getRole.mockReturnValue(null);

    const result = executeGuard({ data: { roles: ['ADMIN'] } } as any, {} as any) as UrlTree;

    expect(router.serializeUrl(result)).toBe('/dashboard/events');
  });
});
