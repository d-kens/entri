import {Routes} from '@angular/router';
import {AuthPage} from './pages/auth-page/auth-page';
import {Login} from './components/login/login';
import {Register} from './components/register/register';
import {noAuthGuard} from '@core/guards/no-auth-guard';
import {ForgotPassword} from '@features/auth/components/forgot-password/forgot-password';
import {ResetPassword} from '@features/auth/components/reset-password/reset-password';

export const AUTH_ROUTES: Routes = [
  {
    path: '',
    component: AuthPage,
    canActivate: [noAuthGuard],
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      { path: 'login', component: Login },
      { path: 'register', component: Register },
      { path: 'forgot-password',component: ForgotPassword },
      { path: 'reset-password',component: ResetPassword }
    ]
  }
]
