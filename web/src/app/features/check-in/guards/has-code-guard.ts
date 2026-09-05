import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { CheckInSessionService } from '../services/check-in-session-service';

export const hasCodeGuard: CanActivateFn = () => {
  const session = inject(CheckInSessionService);
  const router = inject(Router);
  return session.checkInCode() !== null || router.createUrlTree(['/check-in']);
};
