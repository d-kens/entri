import { BasePo } from '../../../../support/base.po';

/**
 * Page Object for the Login page (/auth/login).
 *
 * Wraps all raw cy.get() calls so the tests never need to know
 * about selectors — only about user actions.
 *
 * Usage:
 *   const po = new LoginPo();
 *   po.visit().typeEmail('user@test.com').typePassword('secret').submit();
 */
export class LoginPo extends BasePo {
  visit() {
    cy.visit('/auth/login');
    return this;
  }

  typeEmail(email: string) {
    cy.get('[data-cy="email-input"]').type(email);
    return this;
  }

  typePassword(password: string) {
    cy.get('[data-cy="password-input"]').type(password);
    return this;
  }

  submit() {
    cy.get('[data-cy="login-submit"]').click();
    return this;
  }

  /** Convenience: visit, fill, and submit in one call. */
  loginAs(email: string, password: string) {
    return this.visit().typeEmail(email).typePassword(password).submit();
  }
}
