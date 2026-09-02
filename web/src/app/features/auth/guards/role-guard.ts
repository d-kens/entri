import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '@features/auth/auth-service';

export const roleGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  const allowedRoles: string[] = route.data['roles'] ?? [];
  if (allowedRoles.includes(authService.getRole() ?? '')) return true;
  return router.createUrlTree(['/dashboard/events']);
};
