import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth-service';
import { SnackbarService } from '@shared/services/snackbar-service';
import { catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const snackbar = inject(SnackbarService);
  const accessToken = authService.getToken();

  const isRefreshCall = req.url.includes('/refresh-token');

  const authReq =
    accessToken && !isRefreshCall
      ? req.clone({
          setHeaders: {
            Authorization: `Bearer ${accessToken}`,
          },
        })
      : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isRefreshCall) {
        return authService.refreshToken().pipe(
          catchError((refreshError) => {
            if (refreshError.status === 401 || refreshError.status === 403) {
              authService.clearSession();
              snackbar.showError('Your session has expired. Please log in again.');
              router.navigate(['/auth/login']);
            }
            return throwError(() => refreshError);
          }),
          switchMap((response) => {
            const retryReq = req.clone({
              setHeaders: {
                Authorization: `Bearer ${response.token}`,
              },
            });
            return next(retryReq);
          }),
        );
      }

      return throwError(() => error);
    }),
  );
};
