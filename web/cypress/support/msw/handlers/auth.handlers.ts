import type { MswHandler } from '../handler';

/**
 * MSW handlers for the auth API endpoints.
 *
 * Each handler describes one specific server scenario.
 * Import the ones you need in a *.msw.cy.ts test and pass them to cy.startMsw().
 *
 * Usage:
 *   cy.startMsw([loginUnauthorized]);
 *   cy.startMsw([loginServerError, registerConflict]);
 */

export const loginSuccess: MswHandler = {
  method: 'POST',
  url: '**/auth/login',
  statusCode: 200,
  body: {
    user: {
      externalKey: 'usr_abc123',
      firstName: 'Test',
      lastName: 'User',
      email: 'test@example.com',
      phoneNumber: '0712345678',
      role: 'PLATFORM_USER',
    },
    accessToken: {
      token: 'fake.jwt.token',
      expiresIn: 3600,
    },
  },
  alias: 'loginRequest',
};

export const loginUnauthorized: MswHandler = {
  method: 'POST',
  url: '**/auth/login',
  statusCode: 401,
  body: { message: 'Invalid email or password.' },
  alias: 'loginRequest',
};

export const loginServerError: MswHandler = {
  method: 'POST',
  url: '**/auth/login',
  statusCode: 500,
  body: { message: 'An unexpected error occurred.' },
  alias: 'loginRequest',
};

export const registerConflict: MswHandler = {
  method: 'POST',
  url: '**/auth/register',
  statusCode: 409,
  body: { message: 'An account with this email already exists.' },
  alias: 'registerRequest',
};

export const registerServerError: MswHandler = {
  method: 'POST',
  url: '**/auth/register',
  statusCode: 500,
  body: { message: 'An unexpected error occurred.' },
  alias: 'registerRequest',
};
