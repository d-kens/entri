import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from '../services/auth-service';
import {catchError, switchMap, throwError} from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const accessToken = authService.getToken();

  console.log("----------------------------------------------------------")
  console.log("Request Intercepted");
  console.log(req.url)
  console.log("----------------------------------------------------------")

  // Add access token to the request
  const requestWithAuth = accessToken
    ? req.clone({
        setHeaders: {
          Authorization: `Bearer ${accessToken}`
        }
      })
    : req

  return next(requestWithAuth).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && req.url.includes("/refresh-token")) {
        return authService.refreshToken().pipe(
          switchMap(() => {
            const newAccessToken = authService.getToken();
            const retryReq = req.clone({
              setHeaders: {
                Authorization: `Bearer ${newAccessToken}`
              }
            });
            return next(retryReq);
          }),
          catchError(() => {
            authService.logout();
            return throwError(() => error)
          })
        )
      }

      return throwError(() => error);
    })
  )
};
