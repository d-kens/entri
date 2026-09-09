import { TestBed } from '@angular/core/testing';
import { CanActivateFn, provideRouter, Router, UrlTree } from '@angular/router';

import { hasCodeGuard } from './has-code-guard';
import { CheckInSessionService } from '../services/check-in-session-service';

describe('hasCodeGuard', () => {
  let sessionMock: { checkInCode: ReturnType<typeof vi.fn> };
  let router: Router;

  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => hasCodeGuard(...guardParameters));

  beforeEach(() => {
    sessionMock = { checkInCode: vi.fn() };
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: CheckInSessionService, useValue: sessionMock }],
    });
    router = TestBed.inject(Router);
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });

  it('allows activation when a check-in code is present', () => {
    sessionMock.checkInCode.mockReturnValue('ABC123');

    const result = executeGuard({} as any, {} as any);

    expect(result).toBe(true);
  });

  it('redirects to /check-in when there is no check-in code', () => {
    sessionMock.checkInCode.mockReturnValue(null);

    const result = executeGuard({} as any, {} as any) as UrlTree;

    expect(router.serializeUrl(result)).toBe('/check-in');
  });
});
