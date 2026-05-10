import {BasePo} from '../../../../support/base.po';

export class ForgotPasswordPo extends BasePo {
  visit() {
    cy.visit("/auth/forgot-password");
    return this;
  }

  typeEmail(email: string) {
    cy.get('[data-cy="email-input"]').type(email);
    return this;
  }

  submit() {
    cy.get('[data-cy="forgot-password-submit"]').click();
    return this;
  }
}
