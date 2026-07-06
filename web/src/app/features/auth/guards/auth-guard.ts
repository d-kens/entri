import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../auth-service';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  // Use a fresh check against storage-backed token to avoid stale in-memory state
  if (authService.isAuthenticated()) {
    return true;
  }

  // Return a UrlTree to let the router handle the redirect and preserve the intended URL.
  return router.createUrlTree(['/auth'], { queryParams: { returnUrl: state.url } });
};
