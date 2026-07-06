import { Routes } from '@angular/router';
import { Auth } from './auth';
import { Login } from '@features/auth/login/login';
import { Register } from '@features/auth/register/register';
import { noAuthGuard } from '@features/auth/guards/no-auth-guard';
import { ForgotPassword } from '@features/auth/forgot-password/forgot-password';
import { ResetPassword } from '@features/auth/reset-password/reset-password';

export const AUTH_ROUTES: Routes = [
  {
    path: '',
    component: Auth,
    canActivate: [noAuthGuard],
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      { path: 'login', component: Login },
      { path: 'register', component: Register },
      { path: 'forgot-password', component: ForgotPassword },
      { path: 'reset-password', component: ResetPassword },
    ],
  },
];
