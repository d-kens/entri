import { LoginPo } from './login.po';

/**
 * SMOKE TEST — Login page
 *
 * Fast, high-level check: can the page load and are the key elements present?
 * Does NOT test validation logic or API responses — that's for login.cy.ts and login.msw.cy.ts.
 *
 * Run in isolation: npm run cy:smoke
 */
describe('Login page (smoke)', () => {
  const po = new LoginPo();

  it('loads and shows the login form', () => {
    po.visit();

    cy.get('[data-cy="email-input"]').should('be.visible');
    cy.get('[data-cy="password-input"]').should('be.visible');
    cy.get('[data-cy="login-submit"]').should('be.visible');
  });
});
