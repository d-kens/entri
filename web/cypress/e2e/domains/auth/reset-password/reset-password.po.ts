import {BasePo} from '../../../../support/base.po';

export class ResetPasswordPo extends BasePo {
  visit() {
    cy.visit('/auth/reset-password?token=test-token');
    return this;
  }

  typePassword(password: string) {
    cy.get('[data-cy="password-input"]').type(password);
    return this;
  }

  typeConfirmPassword(password: string) {
    cy.get('[data-cy="confirm-password-input"]').type(password);
    return this;
  }

  submit() {
    cy.get('[data-cy="reset-password-submit"]').click();
    return this;
  }
}
