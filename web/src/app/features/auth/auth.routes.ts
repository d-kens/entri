import {Routes} from '@angular/router';
import {AuthPage} from './pages/auth-page/auth-page';
import {Login} from './components/login/login';
import {Register} from './components/register/register';
import {ResetPassword} from './components/reset-password/reset-password';
import {ForgotPassword} from './components/forgot-password/forgot-password';

export const AUTH_ROUTES: Routes = [
  {
    path: '',
    component: AuthPage,
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      { path: 'login', component: Login },
      { path: 'register', component: Register },
      { path: 'reset-password', component: ResetPassword },
      { path: 'forgot-password', component: ForgotPassword }
    ]
  }
]
