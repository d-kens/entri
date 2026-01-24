import {Routes} from '@angular/router';
import {AuthPage} from './pages/auth-page/auth-page';
import {Login} from './components/login/login';
import {Register} from './components/register/register';
import {ResetPassword} from './components/reset-password/reset-password';
import {ForgotPassword} from './components/forgot-password/forgot-password';
import {ResetPasswordTokenResolver} from '@core/resolvers/reset-password-token.resolver';

export const AUTH_ROUTES: Routes = [
  {
    path: '',
    component: AuthPage,
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      { path: 'login', component: Login },
      { path: 'register', component: Register },
      { path: 'reset-password', component: ResetPassword, resolve: {tokenCheck: ResetPasswordTokenResolver} },
      { path: 'forgot-password', component: ForgotPassword }
    ]
  }
]
