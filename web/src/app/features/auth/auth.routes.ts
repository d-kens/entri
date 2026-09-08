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
      { path: 'login', component: Login, title: 'Sign In' },
      { path: 'register', component: Register, title: 'Create Account' },
      { path: 'forgot-password', component: ForgotPassword, title: 'Forgot Password' },
      { path: 'reset-password', component: ResetPassword, title: 'Reset Password' },
    ],
  },
];
