import { RegisterPo } from './register.po';

/**
 * SMOKE TEST — Register page
 *
 * Run in isolation: npm run cy:smoke
 */
describe('Register page (smoke)', () => {
  const po = new RegisterPo();

  it('loads and shows the registration form', () => {
    po.visit();

    cy.get('[data-cy="first-name-input"]').should('be.visible');
    cy.get('[data-cy="email-input"]').should('be.visible');
    cy.get('[data-cy="register-submit"]').should('be.visible');
  });
});
