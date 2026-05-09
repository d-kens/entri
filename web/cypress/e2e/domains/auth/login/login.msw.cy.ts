import { loginSuccess, loginUnauthorized, loginServerError } from '../../../../support/msw/handlers/auth.handlers';
import { LoginPo } from './login.po';

/**
 * MSW TEST — Login page
 *
 * Uses cy.startMsw() to intercept API calls and return controlled responses.
 * This lets us test error states (401, 500) without needing a real backend
 * in a specific state.
 *
 * Run in isolation: npm run cy:msw
 */
describe('Login page (mocked)', () => {
  const po = new LoginPo();

  describe('when the server returns 200 (valid credentials)', () => {
    beforeEach(() => {
      cy.startMsw([loginSuccess]);
      po.visit();
    });

    it('navigates to the dashboard after a successful login', () => {
      po.loginAs('test@example.com', 'password123');

      cy.wait('@loginRequest');
      po.urlIncludes('/');
      cy.url().should('not.include', '/auth');
    });
  });

  describe('when the server returns 401 (bad credentials)', () => {
    beforeEach(() => {
      cy.startMsw([loginUnauthorized]);
      po.visit();
    });

    it('shows the error message returned by the server', () => {
      po.loginAs('wrong@example.com', 'wrongpassword');

      cy.wait('@loginRequest');
      po.hasText('Invalid email or password.');
    });
  });

  describe('when the server returns 500 (server error)', () => {
    beforeEach(() => {
      cy.startMsw([loginServerError]);
      po.visit();
    });

    it('shows a generic error message', () => {
      po.loginAs('test@example.com', 'password123');

      cy.wait('@loginRequest');
      po.hasText('An unexpected error occurred.');
    });
  });
});
