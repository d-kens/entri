import { BasePo } from '../../../../support/base.po';

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
}
