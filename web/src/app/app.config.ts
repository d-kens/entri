import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withInMemoryScrolling } from '@angular/router';

import { routes } from './app.routes';
import {authInterceptor} from './core/interceptors/auth-interceptor';
import {provideHttpClient, withFetch, withInterceptors} from '@angular/common/http';
import { provideNativeDateAdapter } from '@angular/material/core';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withInMemoryScrolling({ anchorScrolling: 'enabled', scrollPositionRestoration: 'enabled' })),
    provideBrowserGlobalErrorListeners(),
    provideNativeDateAdapter(),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
  ]
};
