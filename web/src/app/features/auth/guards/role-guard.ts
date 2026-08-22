import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { UsersService } from '@features/users/users-service';

export const roleGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const usersService = inject(UsersService);

  const allowedRoles: string[] = route.data['roles'] ?? [];
  const userRole = usersService.currentUser()?.role ?? '';

  if (allowedRoles.includes(userRole)) {
    return true;
  }

  return router.createUrlTree(['/dashboard/summary']);
};
