import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class ResetPasswordTokenResolver implements Resolve<boolean> {

  constructor(private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): boolean {
    const token = route.queryParams['token'];

    if (!token) {
      this.router.navigate(['/auth/login']);
      return false;
    }

    return true;
  }
}
